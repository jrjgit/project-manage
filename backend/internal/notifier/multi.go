package notifier

import (
	"log"
	"sync"
)

// MultiNotifier 组合多个通知渠道，同时向所有渠道发送
type MultiNotifier struct {
	notifiers []Notifier
}

// NewMultiNotifier 创建多通道通知器
func NewMultiNotifier(notifiers ...Notifier) *MultiNotifier {
	return &MultiNotifier{notifiers: notifiers}
}

// Notify 向所有子通知器并发发送，收集错误但不中断
func (m *MultiNotifier) Notify(userID string, message string) error {
	var wg sync.WaitGroup
	for _, n := range m.notifiers {
		wg.Add(1)
		go func(notifier Notifier) {
			defer wg.Done()
			if err := notifier.Notify(userID, message); err != nil {
				log.Printf("[MULTI NOTIFY ERROR] err=%v", err)
			}
		}(n)
	}
	wg.Wait()
	return nil
}

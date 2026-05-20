package notifier

import (
	"log"
	"sync"
)

// AsyncNotifier 异步通知包装器，将同步 Notifier 转为后台执行
type AsyncNotifier struct {
	inner Notifier
	ch    chan *asyncJob
	wg    sync.WaitGroup
	quit  chan struct{}
}

type asyncJob struct {
	userID  string
	message string
}

// NewAsyncNotifier 创建异步通知器，内部启动 goroutine 消费队列
func NewAsyncNotifier(inner Notifier, bufferSize int) *AsyncNotifier {
	if bufferSize <= 0 {
		bufferSize = 100
	}
	a := &AsyncNotifier{
		inner: inner,
		ch:    make(chan *asyncJob, bufferSize),
		quit:  make(chan struct{}),
	}
	a.wg.Add(1)
	go a.worker()
	return a
}

// Notify 将通知任务放入队列，立即返回，不阻塞调用方
func (a *AsyncNotifier) Notify(userID string, message string) error {
	select {
	case a.ch <- &asyncJob{userID: userID, message: message}:
	default:
		// 队列满时降级为同步发送，避免丢消息
		log.Println("[ASYNC NOTIFY] queue full, fallback to sync")
		return a.inner.Notify(userID, message)
	}
	return nil
}

// Close 优雅关闭异步通知器，等待队列消费完毕
func (a *AsyncNotifier) Close() {
	close(a.quit)
	a.wg.Wait()
}

func (a *AsyncNotifier) worker() {
	defer a.wg.Done()
	for {
		select {
		case job := <-a.ch:
			if err := a.inner.Notify(job.userID, job.message); err != nil {
				log.Printf("[ASYNC NOTIFY ERROR] to=%s err=%v", job.userID, err)
			}
		case <-a.quit:
			// 消费完剩余任务后退出
			for {
				select {
				case job := <-a.ch:
					if err := a.inner.Notify(job.userID, job.message); err != nil {
						log.Printf("[ASYNC NOTIFY ERROR] to=%s err=%v", job.userID, err)
					}
				default:
					return
				}
			}
		}
	}
}

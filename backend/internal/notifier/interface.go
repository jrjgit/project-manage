package notifier

// Notifier 通知渠道抽象接口
type Notifier interface {
	// Notify 向指定用户发送消息
	// userID: 目标用户在企业微信中的 userid（对应 User.WechatID）
	// message: 纯文本消息内容
	Notify(userID string, message string) error
}

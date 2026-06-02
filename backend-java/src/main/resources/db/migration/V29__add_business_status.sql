ALTER TABLE requirements ADD COLUMN business_status VARCHAR(50) DEFAULT NULL COMMENT '商务状态: pending_bid-待发标, pending_offer-待报价, bidding-待投标, won-已中标';

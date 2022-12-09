package database

import (
	"fmt"
	"gorm.io/gorm"
)

// IP address record.
// Each address is allocated to a 'Data path ID', which is the identifier of an
// OpenFlow switch

type IpAddress struct {
	Address    string `gorm:"not null;uniqueIndex:uniqueIP;" json:"ip"`
	EthName    string `gorm:"not null;" json:"name"`
	NetMask    uint8  `gorm:"not null;" json:"netmask"`
	DataPathID int64  `gorm:"not null;" json:"dpid"`
}

// This function is called automatically by gorm before saving a record.
// It checks that the netmask is a valid value

func (ip *IpAddress) BeforeSave(tx *gorm.DB) (ret error) {
	ret = nil
	if ip.NetMask > 31 {
		ret = fmt.Errorf("invalid netmask: %d", ip.NetMask)
	}
	return
}

// Converts an IP address and a netmask into a string like '192.90.65.254/24'

func FullAddress(ip *IpAddress) string {
	return fmt.Sprintf("%s/%d", ip.Address, ip.NetMask)
}

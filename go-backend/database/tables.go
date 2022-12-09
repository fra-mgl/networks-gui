package database

// IP address record.
// Each address is allocated to a 'Data path ID', which is the identifier of an
// OpenFlow switch

type IpAddress struct {
	Address    string `gorm:"not null;uniqueIndex:uniqueIP;" json:"ip"`
	EthName    string `gorm:"not null;" json:"name"`
	DataPathID int64  `gorm:"not null;" json:"dpid"`
}

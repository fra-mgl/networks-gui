package database

import (
	"fmt"
	"gorm.io/gorm"
	"strconv"
	"strings"
)

// Database table for OF switches

type DataPath struct {
	ID int64 `gorm:"not null;primary_key;"`
}

// Database table for IP addresses. Each address is assigned to a particular port

type IpAddress struct {
	Address string `gorm:"not null;primary_key;"`
	PortNo  int    `gorm:"not null;"`
	MAC     string `gorm:"not null;"`
}

// Mapping between OF switches, ip addresses assigned to their ports, and related
// next hop addresses.

type PortData struct {
	DataPathID    int64      `gorm:"not null;index:,type:hash"`
	DataPath      DataPath   `gorm:"ForeignKey:DataPathID;"`
	PortAddress   string     `gorm:"not null;uniqueIndex:uniquePortIP"`
	PortAddressFK IpAddress  `gorm:"ForeignKey:PortAddress;"`
	NextHop       string     `gorm:"uniqueIndex:uniqueNextHopIP"`
	NextHopFK     *IpAddress `gorm:"ForeignKey:NextHop;"`
}

// This function is called by Gorm before saving a record of the 'ip_addresses'
// table. It checks that the input ip is of the form '10.0.0.1/24'

func (ip *IpAddress) BeforeSave(tx *gorm.DB) error {
	nip := netMaskedIp{ip.Address}
	return nip.validate()
}

/* LIBRARY CODE TO WORK WITH IP ADDRESSES */

// Given two ip addresses and their netmask, the function returns the ip
// that is part of the larger subnetwork

func largerSubNet(ip1, ip2 ipAddress, net1, net2 int) (ipAddress, int) {
	if net1 > net2 {
		return ip2, net2
	}
	return ip1, net1
}

// The function compares two IP addresses and determines if they are part of
// the same subnetwork

func compareSubNets(ip1, ip2 ipAddress, net1, net2 int) (bool, error) {
	if net1 > 31 {
		return false, fmt.Errorf("invalid netmask: %d", net1)
	}
	if net2 > 31 {
		return false, fmt.Errorf("invalid netmask: %d", net2)
	}

	mask := min(net1, net2)
	binIp1, err := ip1.toBinary()
	if err != nil {
		return false, fmt.Errorf("invalid IP address: %s", ip1.str)
	}
	binIp2, err := ip2.toBinary()
	if err != nil {
		return false, fmt.Errorf("invalid IP address: %s", ip2.str)
	}

	for i := 0; i < mask; i++ {
		if binIp1.str[i] != binIp2.str[i] {
			return false, nil
		}
	}
	return true, nil
}

// Wrapper to a string representation of an IP address of the form 10.0.0.1

type ipAddress struct {
	str string
}

func (ip *ipAddress) validate() error {
	// Check the punctuation
	splittedIp := strings.Split(ip.str, ".")
	if len(splittedIp) != 4 {
		return fmt.Errorf("invalid IP address: %s", ip.str)
	}

	// Check that each number is a valid 8-bit integer
	for i := 0; i < 4; i++ {
		n, err := strconv.Atoi(splittedIp[i])
		if err != nil || n > 255 {
			return fmt.Errorf("invalid IP address: %s", ip.str)
		}
	}
	return nil
}

// Given an IP address and its net mask, it returns the network address.
// 10.0.0.1/24 -> 10.0.0.0/24 . The output IP is in binary representation

func (ip *ipAddress) getNetAddress(net int) (ipAddress, error) {
	if net > 31 {
		return ipAddress{}, fmt.Errorf("invalid net mask: %d", net)
	}
	if err := ip.validate(); err != nil {
		return ipAddress{}, nil
	}

	// The conversion happens using binary representation
	binaryIp, err := ip.toBinary()
	if err != nil {
		return ipAddress{}, err
	}
	binaryNetAddr := binaryIpAddress{""}
	for i := 0; i < net; i++ {
		binaryNetAddr.str = binaryNetAddr.str + binaryIp.str[i:i+1]
	}
	for len(binaryNetAddr.str) < 32 {
		binaryNetAddr.str = binaryNetAddr.str + "0"
	}

	netAddr, err := binaryNetAddr.toIp()
	return netAddr, err
}

// Utility to convert an IP address of the form 10.0.0.1 into its binary equivalent

func (ip *ipAddress) toBinary() (binaryIpAddress, error) {
	if err := ip.validate(); err != nil {
		return binaryIpAddress{}, err
	}

	// The ip string is converted into a 32 bit integer
	splittedIp := strings.Split(ip.str, ".")
	binaryIp := ""
	for i := 0; i < 4; i++ {
		strToInt, _ := strconv.Atoi(splittedIp[i])
		binaryIp = binaryIp + intToStr(strToInt)
	}
	return binaryIpAddress{binaryIp}, nil
}

// Wrapper to a string representation of a network masked IP address of the form
// 10.0.0.1/24

type netMaskedIp struct {
	str string
}

func (nip *netMaskedIp) validate() error {
	// Check that the netmask is present
	splittedIp := strings.Split(nip.str, "/")
	if len(splittedIp) != 2 {
		return fmt.Errorf("missing network mask: %s", nip.str)
	}
	// Check that the netmask is a number between 0 and 31
	netMask, err := strconv.Atoi(splittedIp[1])
	if err != nil || netMask > 31 || netMask < 0 {
		return fmt.Errorf("invalid network mask: %s", nip.str)
	}
	// Check that the actual ip address is valid
	ip := ipAddress{nip.str}
	if err := ip.validate(); err != nil {
		return err
	}
	return nil
}

// The function converts an IP address of the form 10.0.0.1/24 into (10.0.0.1, 24),
// where the output ip is string representing a binary digit

func (nip *netMaskedIp) splitIpAndNetMask() (ipAddress, int, error) {
	if err := nip.validate(); err != nil {
		return ipAddress{}, 0, err
	}
	splittedIp := strings.Split(nip.str, "/")
	ip := ipAddress{splittedIp[0]}
	netMask, err := strconv.Atoi(splittedIp[1])
	return ip, netMask, err
}

// Wrapper to a string representation of a binary IP address

type binaryIpAddress struct {
	str string
}

func (bip *binaryIpAddress) validate() error {
	// Check the length of the string
	if len(bip.str) != 32 {
		return fmt.Errorf("invalid binary IP address: %s", bip.str)
	}
	// Check that the string is a valid 32 bit binary number
	for i := 0; i < 32; i++ {
		if bip.str[i:i+1] != "0" && bip.str[i:i+1] != "1" {
			return fmt.Errorf("invalid binary IP address: %s", bip.str)
		}
	}
	return nil
}

// Converts the binary representation of an IP address into the common numeric representation

func (bip *binaryIpAddress) toIp() (ipAddress, error) {
	if err := bip.validate(); err != nil {
		return ipAddress{}, err
	}

	ip := ""
	for i := 0; i < 4; i++ {
		if i != 0 {
			ip = ip + "."
		}
		currDigit, err := binaryStrToInt(bip.str[8*i : 8*(i+1)])
		if err != nil {
			return ipAddress{}, err
		}
		ip = ip + strconv.Itoa(currDigit)
	}
	return ipAddress{ip}, nil
}

// Utility to convert an integer into its binary representation, as a string

func intToStr(u8 int) string {
	res := ""
	tmp := u8
	for tmp > 1 {
		rem := tmp % 2
		tmp = tmp >> 1 // tmp / 2
		res = strconv.Itoa(rem) + res
	}
	res = strconv.Itoa(tmp) + res

	for len(res) < 8 {
		res = "0" + res
	}
	return res
}

// Utility to convert a string representation of a binary digit into an integer

func binaryStrToInt(str string) (int, error) {
	res := 0
	for i := 0; i < len(str); i++ {
		currDigitStr := str[len(str)-i-1 : len(str)-i]
		currDigit, err := strconv.Atoi(currDigitStr)
		if err != nil {
			return 0, err
		}
		res += currDigit * (1 << i)
	}
	return res, nil
}

func min(a, b int) int {
	if a > b {
		return b
	}
	return a
}

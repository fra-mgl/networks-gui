package netconf

import (
	"fmt"
	"strconv"
	"strings"
)

/* LIBRARY CODE TO WORK WITH IP ADDRESSES */

// Given two ip addresses and their netmask, the function returns the ip
// that is part of the larger subnetwork

func LargerSubNet(ip1, ip2 IpAddress, net1, net2 int) (IpAddress, int) {
	if net1 > net2 {
		return ip2, net2
	}
	return ip1, net1
}

// The function compares two IP addresses and determines if they are part of
// the same subnetwork

func CompareSubNets(ip1, ip2 IpAddress, net1, net2 int) (bool, error) {
	if net1 > 31 {
		return false, fmt.Errorf("invalid netmask: %d", net1)
	}
	if net2 > 31 {
		return false, fmt.Errorf("invalid netmask: %d", net2)
	}

	mask := min(net1, net2)
	binIp1, err := ip1.ToBinary()
	if err != nil {
		return false, fmt.Errorf("invalid IP address: %s", ip1.Str)
	}
	binIp2, err := ip2.ToBinary()
	if err != nil {
		return false, fmt.Errorf("invalid IP address: %s", ip2.Str)
	}

	for i := 0; i < mask; i++ {
		if binIp1.Str[i] != binIp2.Str[i] {
			return false, nil
		}
	}
	return true, nil
}

// Wrapper to a string representation of an IP address of the form 10.0.0.1

type IpAddress struct {
	Str string
}

func (ip *IpAddress) Validate() error {
	// Check the punctuation
	splittedIp := strings.Split(ip.Str, ".")
	if len(splittedIp) != 4 {
		return fmt.Errorf("invalid IP address: %s", ip.Str)
	}

	// Check that each number is a valid 8-bit integer
	for i := 0; i < 4; i++ {
		n, err := strconv.Atoi(splittedIp[i])
		if err != nil || n > 255 {
			return fmt.Errorf("invalid IP address: %s", ip.Str)
		}
	}
	return nil
}

// Given an IP address and its net mask, it returns the network address.
// 10.0.0.1/24 -> 10.0.0.0/24 . The output IP is in binary representation

func (ip *IpAddress) GetNetAddress(net int) (IpAddress, error) {
	if net > 31 {
		return IpAddress{}, fmt.Errorf("invalid net mask: %d", net)
	}
	if err := ip.Validate(); err != nil {
		return IpAddress{}, nil
	}

	// The conversion happens using binary representation
	binaryIp, err := ip.ToBinary()
	if err != nil {
		return IpAddress{}, err
	}
	binaryNetAddr := BinaryIpAddress{""}
	for i := 0; i < net; i++ {
		binaryNetAddr.Str = binaryNetAddr.Str + binaryIp.Str[i:i+1]
	}
	for len(binaryNetAddr.Str) < 32 {
		binaryNetAddr.Str = binaryNetAddr.Str + "0"
	}

	netAddr, err := binaryNetAddr.ToIp()
	return netAddr, err
}

// Utility to convert an IP address of the form 10.0.0.1 into its binary equivalent

func (ip *IpAddress) ToBinary() (BinaryIpAddress, error) {
	if err := ip.Validate(); err != nil {
		return BinaryIpAddress{}, err
	}

	// The ip string is converted into a 32 bit integer
	splittedIp := strings.Split(ip.Str, ".")
	binaryIp := ""
	for i := 0; i < 4; i++ {
		strToInt, _ := strconv.Atoi(splittedIp[i])
		binaryIp = binaryIp + IntToStr(strToInt)
	}
	return BinaryIpAddress{binaryIp}, nil
}

// Wrapper to a string representation of a network masked IP address of the form
// 10.0.0.1/24

type NetMaskedIp struct {
	Str string
}

func (nip *NetMaskedIp) Validate() error {
	// Check that the netmask is present
	splittedIp := strings.Split(nip.Str, "/")
	if len(splittedIp) != 2 {
		return fmt.Errorf("missing network mask: %s", nip.Str)
	}
	// Check that the netmask is a number between 0 and 31
	netMask, err := strconv.Atoi(splittedIp[1])
	if err != nil || netMask > 31 || netMask < 0 {
		return fmt.Errorf("invalid network mask: %s", nip.Str)
	}
	// Check that the actual ip address is valid
	ip := IpAddress{splittedIp[0]}
	if err := ip.Validate(); err != nil {
		return err
	}
	return nil
}

// The function converts an IP address of the form 10.0.0.1/24 into (10.0.0.1, 24),
// where the output ip is string representing a binary digit

func (nip *NetMaskedIp) SplitIpAndNetMask() (IpAddress, int, error) {
	if err := nip.Validate(); err != nil {
		return IpAddress{}, 0, err
	}
	splittedIp := strings.Split(nip.Str, "/")
	ip := IpAddress{splittedIp[0]}
	netMask, err := strconv.Atoi(splittedIp[1])
	return ip, netMask, err
}

// Given an IP address and its net mask, it returns the network address.
// 10.0.0.1/24 -> 10.0.0.0/24 . The output IP is in binary representation

func (nip *NetMaskedIp) GetNetAddress() (NetMaskedIp, error) {
	if err := nip.Validate(); err != nil {
		return NetMaskedIp{}, err
	}
	rawIp, netMask, err := nip.SplitIpAndNetMask()
	if err != nil {
		return NetMaskedIp{}, err
	}
	rawNetIp, err := rawIp.GetNetAddress(netMask)
	if err != nil {
		return NetMaskedIp{}, err
	}
	return NetMaskedIp{rawNetIp.Str + "/" + strconv.Itoa(netMask)}, nil
}

// Wrapper to a string representation of a binary IP address

type BinaryIpAddress struct {
	Str string
}

func (bip *BinaryIpAddress) Validate() error {
	// Check the length of the string
	if len(bip.Str) != 32 {
		return fmt.Errorf("invalid binary IP address: %s", bip.Str)
	}
	// Check that the string is a valid 32 bit binary number
	for i := 0; i < 32; i++ {
		if bip.Str[i:i+1] != "0" && bip.Str[i:i+1] != "1" {
			return fmt.Errorf("invalid binary IP address: %s", bip.Str)
		}
	}
	return nil
}

// Converts the binary representation of an IP address into the common numeric representation

func (bip *BinaryIpAddress) ToIp() (IpAddress, error) {
	if err := bip.Validate(); err != nil {
		return IpAddress{}, err
	}

	ip := ""
	for i := 0; i < 4; i++ {
		if i != 0 {
			ip = ip + "."
		}
		currDigit, err := BinaryStrToInt(bip.Str[8*i : 8*(i+1)])
		if err != nil {
			return IpAddress{}, err
		}
		ip = ip + strconv.Itoa(currDigit)
	}
	return IpAddress{ip}, nil
}

// Utility to convert an integer into its binary representation, as a string

func IntToStr(u8 int) string {
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

func BinaryStrToInt(str string) (int, error) {
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

// Utility to convert an hexadecimal string into an integer

func HexStrToInt(str string) (int64, error) {
	var res int64 = 0
	for i := 0; i < len(str); i++ {
		currDigitStr := str[len(str)-i-1 : len(str)-i]
		var currDigit int
		switch currDigitStr {
		case "F", "f":
			currDigit = 15
		case "E", "e":
			currDigit = 14
		case "D", "d":
			currDigit = 13
		case "C", "c":
			currDigit = 12
		case "B", "b":
			currDigit = 11
		case "A", "a":
			currDigit = 10
		default:
			digit, err := strconv.Atoi(currDigitStr)
			if err != nil {
				return 0, err
			}
			currDigit = digit
		}
		res += currDigit * (1 << i * 4)
	}
	return res, nil
}

func min(a, b int) int {
	if a > b {
		return b
	}
	return a
}

package database

import (
	"gorm.io/driver/postgres"
	"gorm.io/gorm"
	"os"
)

var dbName = os.Getenv("POSTGRES_DB")
var dbUser = os.Getenv("POSTGRES_USER")
var dbPassword = os.Getenv("POSTGRES_PASSWORD")
var dbHost = os.Getenv("POSTGRES_HOST")
var dbPort = os.Getenv("POSTGRES_PORT")
var dbURL = "sslmode=disable user=" + dbUser + " password=" + dbPassword + " dbname=" +
	dbName + " host=" + dbHost + " port=" + dbPort

func InitDB() *DbConn {
	// A Gorm database connection is created
	gormDB, err := gorm.Open(postgres.Open(dbURL), &gorm.Config{})
	if err != nil {
		panic(err)
	}

	// Migrations are applied. That means that overall changes to the db data model are applied
	err = gormDB.AutoMigrate(&IpAddress{})

	dbConn := new(DbConn)
	dbConn.gormConn = gormDB
	return dbConn
}

package database

import (
	"database/sql"
	"gorm.io/driver/postgres"
	"gorm.io/gorm"
	"os"
)

var dbName = os.Getenv("POSTGRES_USER")
var dbUser = os.Getenv("POSTGRES_USER")
var dbPassword = os.Getenv("POSTGRES_PASSWORD")
var dbHost = "localhost"
var dbPort = "5432"
var dbURL = "sslmode=disable user=" + dbUser + " password=" + dbPassword + " dbname=" +
	dbName + " host=" + dbHost + " port=" + dbPort

func InitDB() *DbConn {
	// The SQL driver opens a connection to the database
	db, err := sql.Open("pgx", dbURL)
	if err != nil {
		panic(err)
	}

	// A Gorm database connection is created
	gormDB, err := gorm.Open(postgres.New(postgres.Config{
		Conn: db,
	}), &gorm.Config{})
	if err != nil {
		panic(err)
	}

	// Migrations are applied. That means that overall changes to the db data model are applied
	err = gormDB.AutoMigrate([]interface{}{IpAddress{}})

	dbConn := new(DbConn)
	dbConn.gormConn = gormDB
	return dbConn
}

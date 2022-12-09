# syntax=docker/dockerfile:1
FROM golang:1.19
WORKDIR /go/src/go-backend
COPY . .
RUN go build -o ./main cmd/main.go

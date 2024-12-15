# Hangout Auth API

## Build Checks

[![CodeQL](https://github.com/opticSquid/hangout-auth-service/actions/workflows/github-code-scanning/codeql/badge.svg)](https://github.com/opticSquid/hangout-auth-service/actions/workflows/github-code-scanning/codeql)
[![Java CI with Maven](https://github.com/opticSquid/hangout-auth-service/actions/workflows/maven.yml/badge.svg)](https://github.com/opticSquid/hangout-auth-service/actions/workflows/maven.yml)
[![Docker Image CI](https://github.com/opticSquid/hangout-auth-service/actions/workflows/docker-image-build.yml/badge.svg)](https://github.com/opticSquid/hangout-auth-service/actions/workflows/docker-image-build.yml)
[![Docker Image CD](https://github.com/opticSquid/hangout-auth-service/actions/workflows/docker-image-publish.yml/badge.svg)](https://github.com/opticSquid/hangout-auth-service/actions/workflows/docker-image-publish.yml)

## Introduction

Authentication and Authorization service provider API for Hangout.

This service can maintain multi device logins for every user including untrusted sessions where only limited capability of the whole platform will be enabled. It handles from signup to login to renewing access from time to time to letting other services validate the user to let them do tasks which require to check if the user is valid. Authorization is done through JWT tokens which is issued per device basis so that every user can maintain simultaneous login sessions in multiple devices. It also has the capability to track user devices without invading privacy

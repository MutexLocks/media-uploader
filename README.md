# Media Uploader

<div align="center">
🌍 Language | 语言选择:

[English](README.md) | [简体中文](README_ZH.md)
</div>

<img src="docs/images/logo.png" style="width:6%;"  alt="logo"/>Media Uploader is a media uploading component for [UTMatrix](https://www.utmatrix.top), designed for one-click uploading of videos/articles to multiple platforms. This component can be used together with the [UTMatrix](https://www.utmatrix.top) system.
<br>
UTMatrix Official Website: [https://www.utmatrix.top](https://www.utmatrix.top)
<br>
UTMatrix Online DEMO: [https://demo.utmatrix.top](https://demo.utmatrix.top)
<br>
[![Version](https://img.shields.io/badge/version-v1.0.0-blue)]()
[![License](https://img.shields.io/badge/license-MIT-green)]()
[![Language](https://img.shields.io/badge/language-Java-orange)]()
[![Framework](https://img.shields.io/badge/framework-Selenium-brightgreen)]()
[![Build Tool](https://img.shields.io/badge/build-Maven-blueviolet)]()
[![FAQ](https://img.shields.io/badge/FAQ-included-important)]()
[![Status](https://img.shields.io/badge/status-maintained-brightgreen)]()

## Table of Contents

- [Component Description](#component-description)
  - [System Architecture](#system-architecture)
  - [Add Account Process](#add-account-process)
  - [Video Upload Process](#video-upload-process)
- [Features](#features)
- [Requirements](#requirements)
- [Add New Uploader](#add-new-uploader)
  - [Add New Video Upload Platform](#add-new-video-upload-platform)
    - [Add Logo](#1-add-logo)
    - [Implement Interface](#2-implement-interface)
    - [Register SPI Service](#3-register-spi-service)
  - [Add New Article Upload Platform](#add-new-article-upload-platform)
    - [Add Logo](#1-add-logo-1)
    - [Implement Interface](#2-implement-interface-1)
    - [Register SPI Service](#3-register-spi-service-1)
- [Installation](#installation)
- [Debugging](#debugging)
- [Contributing](#contributing)
- [FAQ](#faq)
- [Contact](#contact)
- [Acknowledgements](#acknowledgements)

## Component Description
### System Architecture
This component is the official uploader for UTMatrix, providing complete support for media file uploads.

![Architecture](docs/images/meida-uploader.svg)

### 1. Add Account Process
![Account](docs/images/login.svg)
### 2. Video Upload Process
![publish](docs/images/publish.svg)

## Features

- Platform logo display
- Username and avatar recognition
- Video/document upload to different platforms

## Requirements
- Java 8
- selenium 4

## Add New Uploader

This project uses the SPI mechanism to support custom uploader extensions. You can add a new uploader by following these steps:

### 1. Add New Video Upload Platform

#### 1) Add Logo
Place the new platform's logo in `src/main/resources/logo` (it will be referenced in step 2's interface implementation).

#### 2) Implement Interface
Create a new uploader class and implement the [VideoUploader](https://gitee.com/when-bean/uploader/blob/master/src/main/java/com/g/uploader/VideoUploader.java) interface. You can refer to [BilibiliVideoUploader.java](src%2Fmain%2Fjava%2Fcom%2Fg%2Fmedia%2Fuploader%2Fcomponent%2Fvideo%2FBilibiliVideoUploader.java) as an example.

#### 3) Register SPI Service

In [com.g.uploader.VideoUploader](src%2Fmain%2Fresources%2FMETA-INF%2Fservices%2Fcom.g.uploader.VideoUploader), add your new platform class at the end:
> com.g.media.uploader.component.video.BilibiliVideoUploader
com.g.media.uploader.component.video.WechatVideoUploader
com.g.media.uploader.component.video.DouYinVideoUploader
com.g.media.uploader.component.video.KuaiShouVideoUploader
com.g.media.uploader.component.video.WeiShiVideoUploader
com.g.media.uploader.component.video.XiaoHongShuVideoUploader
> **_com.g.media.uploader.component.video.YourVideoUploader_**

### 2. Add New Article Upload Platform

#### 1) Add Logo
Place the new platform's logo in `src/main/resources/logo` (it will be referenced in step 2's interface implementation).

#### 2) Implement Interface
Create a new uploader class and implement the [ArticleUploader](https://gitee.com/when-bean/uploader/blob/master/src/main/java/com/g/uploader/ArticleUploader.java) interface. You can refer to [DaYuArticleUploader.java](src%2Fmain%2Fjava%2Fcom%2Fg%2Fmedia%2Fuploader%2Fcomponent%2Farticle%2FDaYuArticleUploader.java) as an example.

#### 3) Register SPI Service

In [com.g.uploader.ArticleUploader](src%2Fmain%2Fresources%2FMETA-INF%2Fservices%2Fcom.g.uploader.ArticleUploader), add your new platform class at the end:
> com.g.media.uploader.component.article.SouHuArticleUploader
com.g.media.uploader.component.article.TouTiaoHaoArticleUploader
com.g.media.uploader.component.article.ZhiHuArticleUploader
com.g.media.uploader.component.article.DaYuArticleUploader
> **_com.g.media.uploader.component.video.YourArticleUploader_**

## Installation
#### 1) Build Jar
Use Maven to package the project and obtain a jar file.

#### 2) Import File
Log in to UTMatrix, go to **System Settings** -> **System Upgrade** -> **Custom Component Upgrade**, and import the packaged jar file.
![update](docs/images/update.png)
#### 3) Verify Import
Log in to UTMatrix, go to **Account Management**, click Add Account, and you should see the logo of the newly added platform.
![account](docs/images/accounts.png)

## Debugging
#### 1) Enable DEBUG Mode
Enter the VNC interface of UTMatrix, select "1" to enable DEBUG mode, set the remote debug address and port (5005) in IDEA, and you can debug with breakpoints.
![debug](docs/images/visit.png)

## Contributing

1. Fork this repository
2. Create a new branch
3. Commit your changes
4. Open a Pull Request

## FAQ

### Q: How to extend page parameters?
A: When uploading media files, there is an option **Other Settings**, which corresponds to the `extraConfig` method in the interface. You can parse and handle this parameter as needed.

### Q: How to debug the code?
A: Please refer to the [Debugging](#debugging) section.

## Contact
- Official Website: https://www.utmatrix.top
- Email: whenbean@foxmail.com

## Acknowledgements

Thanks to all contributors for supporting this project! 
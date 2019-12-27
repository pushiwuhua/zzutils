/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("unused")

package com.pushiwuhua.zzutilslib

/**
 * 常量配置
 * wzz created at 2019/12/27 14:04
 */
const val PAGESIZE = 10//默认的分页加载数
const val OVERTIME = 20L//服务器接口超时时间配置
const val MAX_PAGESIZE = 10000//服务器偷懒, 不返回分页加载
const val LOCAL_PAGESIZE = 10000//本地的分页加载数

const val Permission_REQ_CODE_LOCATION = 110 //请求位置权限
const val Permission_REQ_CODE_READ_PHONE_SATE = 111 //请求设备状态权限
const val Permission_REQ_CODE_WRITE_EXTERNAL_STORAGE = 112 //请求设备存储权限
const val Permission_REQ_CODE_OVERLAY = 113 //悬浮窗权限
const val Permission_REQ_CODE_BLUETOOTH = 114 //蓝牙权限
const val Permission_REQ_CODE_NFC = 115 //蓝牙权限
const val Permission_REQ_CODE_GPS = 116 //GPS权限
const val Permission_REQ_IGNORE_BATTERY_IGNORE = 117 //忽略电量优化



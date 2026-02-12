#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
远程触摸控制客户端
通过WiFi连接Android手机,实现鼠标控制手机触摸
"""

import socket
import time
from pynput import mouse
import sys


class RemoteTouchClient:
    def __init__(self, phone_ip, phone_port=8888):
        self.phone_ip = phone_ip
        self.phone_port = phone_port
        self.socket = None
        self.is_connected = False

        # 屏幕分辨率映射(根据实际情况调整)
        self.laptop_width = 1920
        self.laptop_height = 1080
        self.phone_width = 1080
        self.phone_height = 2340

    def connect(self):
        """连接到手机"""
        try:
            self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.socket.settimeout(5)
            self.socket.connect((self.phone_ip, self.phone_port))
            self.is_connected = True
            print(f"✓ 已连接到手机: {self.phone_ip}:{self.phone_port}")
            return True
        except Exception as e:
            print(f"✗ 连接失败: {e}")
            return False

    def disconnect(self):
        """断开连接"""
        if self.socket:
            self.socket.close()
            self.is_connected = False
            print("已断开连接")

    def send_command(self, command):
        """发送指令"""
        if not self.is_connected:
            print("未连接到手机")
            return False

        try:
            self.socket.sendall((command + "\n").encode('utf-8'))
            return True
        except Exception as e:
            print(f"发送失败: {e}")
            self.is_connected = False
            return False

    def map_coordinates(self, x, y):
        """将笔记本坐标映射到手机坐标"""
        phone_x = int(x * self.phone_width / self.laptop_width)
        phone_y = int(y * self.phone_height / self.laptop_height)

        # 确保坐标在范围内
        phone_x = max(0, min(phone_x, self.phone_width - 1))
        phone_y = max(0, min(phone_y, self.phone_height - 1))

        return phone_x, phone_y

    def click(self, x, y):
        """点击指定坐标"""
        phone_x, phone_y = self.map_coordinates(x, y)
        return self.send_command(f"CLICK:{phone_x},{phone_y}")

    def move(self, x, y):
        """移动到指定坐标"""
        phone_x, phone_y = self.map_coordinates(x, y)
        return self.send_command(f"MOVE:{phone_x},{phone_y}")

    def swipe(self, x1, y1, x2, y2, duration_ms=300):
        """滑动"""
        px1, py1 = self.map_coordinates(x1, y1)
        px2, py2 = self.map_coordinates(x2, y2)
        return self.send_command(f"SWIPE:{px1},{py1},{px2},{py2},{duration_ms}")


class MouseController:
    def __init__(self, phone_ip):
        self.client = RemoteTouchClient(phone_ip)
        self.is_running = False

    def start(self):
        """启动鼠标监听"""
        if not self.client.connect():
            return

        self.is_running = True
        print("\n=== 远程触摸控制已启动 ===")
        print("鼠标移动和点击将同步到手机")
        print("按 Ctrl+C 退出\n")

        # 监听鼠标事件
        with mouse.Listener(
            on_move=self.on_move,
            on_click=self.on_click
        ) as listener:
            try:
                listener.join()
            except KeyboardInterrupt:
                print("\n正在退出...")
                self.stop()

    def stop(self):
        """停止监听"""
        self.is_running = False
        self.client.disconnect()

    def on_move(self, x, y):
        """鼠标移动事件"""
        if self.is_running:
            self.client.move(x, y)

    def on_click(self, x, y, button, pressed):
        """鼠标点击事件"""
        if self.is_running and pressed and button == mouse.Button.left:
            print(f"点击: ({x}, {y})")
            self.client.click(x, y)


def test_connection(phone_ip):
    """测试连接"""
    print(f"正在测试连接到 {phone_ip}:8888...")
    client = RemoteTouchClient(phone_ip)

    if client.connect():
        print("连接成功!")

        # 测试点击
        print("测试点击屏幕中心...")
        client.click(960, 540)
        time.sleep(1)

        # 测试滑动
        print("测试向右滑动...")
        client.swipe(100, 1000, 900, 1000, 300)

        client.disconnect()
        return True
    else:
        return False


def print_usage():
    """打印使用说明"""
    print("""
远程触摸控制客户端 v1.0

用法:
  测试连接: python remote_touch_client.py test <手机IP>
  启动控制: python remote_touch_client.py start <手机IP>

示例:
  python remote_touch_client.py test 192.168.1.100
  python remote_touch_client.py start 192.168.1.100

注意事项:
  1. 确保手机和电脑连接同一WiFi网络
  2. 手机端需要先安装APK并启动服务
  3. 手机端需要开启无障碍服务
  4. 需要安装依赖: pip install pynput
""")


if __name__ == "__main__":
    if len(sys.argv) < 2:
        print_usage()
        sys.exit(1)

    command = sys.argv[1]

    if command in ["test", "start"]:
        if len(sys.argv) < 3:
            print("错误: 缺少手机IP地址")
            print_usage()
            sys.exit(1)

        phone_ip = sys.argv[2]

        if command == "test":
            test_connection(phone_ip)
        elif command == "start":
            controller = MouseController(phone_ip)
            controller.start()
    else:
        print(f"未知命令: {command}")
        print_usage()
        sys.exit(1)

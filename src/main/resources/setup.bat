@echo off
reg add HKEY_CURRENT_USER\Software\Google\Chrome\NativeMessagingHosts\so.zjd.sstk /ve /t REG_SZ /d %~dp0\SimpleSendToKindle.json /f

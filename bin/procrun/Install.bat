BASService //IS//BASService ^
--StartPath=D:\BAS2019 ^
--Classpath=D:\BAS2019\core.dll ^
--Startup=auto ^
--StartMode=jvm --StartClass=app.Standalone ^
--StartMethod=start ^
--StopMode=jvm --StopClass=app.Standalone ^
--StopMethod=stop ^
--StopPath=D:\ ^
--LogLevel=Error ^
--LogPrefix=BASService ^
--LogPath=D:\BAS2019\logs ^
--StdOutput=auto ^
--StdError=auto ^
--Jvm="C:\Program Files\Java\jre1.8.0_221\bin\server\jvm.dll" ^
--JvmMs=256 ^
--JvmMx=1024 ^
--JvmSs=4000 ^
++JvmOptions=-Dapp_config.properties=D:\BAS2019\app.properties ^
++JvmOptions=-Denv.connections.file=D:\BAS2019\database.properties
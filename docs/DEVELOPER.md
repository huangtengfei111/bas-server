# 开发手册

## 技术栈

* ActiveWeb
* ActiveJDBC
* FreeMarker
* Guice
* DB Migrator
* Shiro
* MySQL
* 项目工具
  * Maven
  * PostMan
  * Eclipse
* 核心工具库
  * guava

## 开始开发

1. 新建数据库: `./scripts/remigrate.sh`
1. 启动服务: `./scripts/clean_run.sh`
1. 生成eclipse项目配置: `mvn eclipse:clean; mvn eclipse:eclipse`

## BUILD

1. 设置NodeJS和Java的版本

```
nvm use v11.14.0
sdk use java 8.0.212-zulu
```

1. 编译web

```
cd ~/build/bas
yarn build
cp -r build/* ~/build/bas-server/src/main/resources/webroot/
```

1. 打包jar文件

```
cd ~/build/bas-server
git checkout xxx_branch
git pull
mvn package
```

## 资料

* http://javalite.io/documentation
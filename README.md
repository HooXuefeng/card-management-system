# 饭卡管理系统

一个基于Spring Boot和Vue.js的饭卡管理系统，支持用户管理、卡片管理、充值、消费等功能。

## 系统架构

- **后端**: Spring Boot + MySQL + Redis
- **前端**: Vue.js + Vite
- **部署**: Docker + Docker Compose

## 功能特性

- 用户注册、登录、密码修改
- 饭卡绑定、查询、冻结、解冻
- 饭卡充值、消费记录查询
- 用户自助冻结/解冻饭卡
- 管理员后台管理功能
- 分布式锁保证并发安全
- Redis缓存提升性能

## 快速开始

### 环境要求

- Docker
- Docker Compose

### 部署步骤

1. 克隆项目到本地
2. 进入项目目录
3. 运行部署脚本

**Windows用户:**
```bash
deploy.bat
```

**Linux/Mac用户:**
```bash
chmod +x deploy.sh
./deploy.sh
```

### 手动部署

如果自动部署脚本不适用，也可以手动执行以下命令：

```bash
# 构建并启动所有服务
docker-compose up --build -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

## 访问系统

- **前端访问地址**: http://localhost
- **后端API地址**: http://localhost:8080
- **MySQL端口**: 3306
- **Redis端口**: 6379

## 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 测试用户 | user1 | user123 |
| 测试用户 | user2 | user123 |

## 开发环境搭建

### 后端开发

1. 进入card-backend目录
2. 确保已安装Java 17和Maven
3. 修改application.yml中的数据库和Redis配置
4. 运行以下命令启动后端服务

```bash
mvn spring-boot:run
```

### 前端开发

1. 进入card-frontend目录
2. 确保已安装Node.js 18+
3. 修改src/utils/request.js中的API地址
4. 运行以下命令启动前端服务

```bash
npm install
npm run dev
```

## API文档

### 用户相关

- POST /user/login - 用户登录
- POST /user/register - 用户注册
- POST /user/password - 修改密码

### 卡片相关

- GET /card/info/{cardId} - 获取卡片信息
- POST /card/recharge - 充值
- POST /card/consume - 消费
- POST /card/freeze - 冻结卡片
- POST /card/unfreeze - 解冻卡片
- POST /card/status/freeze - 用户自助冻结
- POST /card/status/unfreeze - 用户自助解冻

## 测试

### 运行单元测试

```bash
# 后端测试
cd card-backend
mvn test

# 前端测试
cd card-frontend
npm run test
```

## 项目结构

```
Card/
├── card-backend/          # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/      # Java源代码
│   │   │   └── resources/ # 配置文件
│   │   └── test/          # 测试代码
│   ├── Dockerfile         # 后端Docker配置
│   └── pom.xml            # Maven配置
├── card-frontend/         # 前端项目
│   ├── src/               # Vue源代码
│   ├── dist/              # 构建输出
│   ├── Dockerfile         # 前端Docker配置
│   └── package.json       # NPM配置
├── mysql/                 # 数据库初始化脚本
│   └── init.sql
├── docker-compose.yml     # Docker Compose配置
├── deploy.sh              # Linux/Mac部署脚本
├── deploy.bat             # Windows部署脚本
└── README.md              # 项目说明文档
```

## 常见问题

### 1. 端口冲突

如果80或8080端口已被占用，可以修改docker-compose.yml中的端口映射。

### 2. 数据库连接失败

检查MySQL容器是否正常启动，以及数据库配置是否正确。

### 3. Redis连接失败

检查Redis容器是否正常启动，以及Redis配置是否正确。

### 4. 前端无法访问后端

检查nginx配置中的API代理设置，以及后端服务是否正常启动。

## 贡献

欢迎提交Issue和Pull Request来改进这个项目。

## 许可证

MIT License
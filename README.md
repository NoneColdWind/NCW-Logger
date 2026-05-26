
# NCW-Logger

[English](#ncw-logger-english) | [中文](#ncw-logger-中文)

---

## NCW-Logger (English)

A lightweight, high-performance Java logging library with rich features.

### Features

- **SLF4J-style placeholder formatting** - `{}` parameter replacement
- **MDC context support** - Thread-local diagnostic context (user ID, request ID, etc.)
- **Multiple layouts** - PatternLayout, JsonLayout, SimpleLayout
- **Multiple appenders** - ConsoleAppender (color support), FileAppender (async writing, rolling)
- **Log configuration** - Properties file loading
- **Log metrics** - Count statistics, throughput, dropped events
- **Caller info extraction** - Auto-detect class/method/line number
- **Exception formatting** - Beautiful stack trace formatting
- **Hierarchical loggers** - Logger inheritance

### Quick Start

#### Basic Usage

```java
import cn.ncw.logger.log.NCWLogger;
import cn.ncw.logger.log.LEVEL;

NCWLogger logger = NCWLogger.getLogger("MyApp");
logger.setLevel(LEVEL.DEBUG);

logger.info("Application started");
logger.debug("User {} logged in", "Alice");
logger.warn("Warning: memory usage high");
logger.error("Error occurred", new RuntimeException("Test exception"));
```

#### MDC Context

```java
import cn.ncw.logger.log.MDC;

MDC.put("userId", "12345");
MDC.put("requestId", "req-abc-123");
logger.info("Processing request");
MDC.clear();
```

#### JSON Output

```java
NCWLogger jsonLogger = NCWLogger.getLogger("JsonLogger");
jsonLogger.clearAppenders();
jsonLogger.addAppender(new Appender.ConsoleAppender("json", 
    new Layout.JsonLayout(true, true, true)) {});
```

#### File Appender

```java
Appender.FileAppender fileAppender = Appender.file("file", 
    new Layout.PatternLayout("%d %p [%t] %c - %m%n"),
    "./logs", "app_{timestamp}.log");
logger.addAppender(fileAppender);
```

### Architecture

```
┌─────────────────────────────────────────────────┐
│         NCWLogger (Facade)                     │
└────────────────┬────────────────────────────────┘
                 │
    ┌────────────┴────────────┐
    │                         │
┌───▼────────┐        ┌──────▼───────┐
│  Appenders │        │  Metrics     │
├────────────┤        └──────────────┘
│ Console    │
│ File       │
│ Custom     │
└────────────┘
       │
┌──────▼───────┐
│   Layouts    │
├──────────────┤
│ Pattern      │
│ Json         │
│ Simple       │
└──────────────┘
```

### Configuration

#### Log Levels

| Level | Description |
|-------|-------------|
| TRACE | Most verbose debugging info |
| DEBUG | Debug information |
| INFO | General information |
| WARN | Warning messages |
| ERROR | Error conditions |
| FATAL | Severe errors |
| OFF | Disabled |

#### Pattern Layout Placeholders

| Placeholder | Description |
|-------------|-------------|
| `%d`, `%date` | Date |
| `%t`, `%time` | Time |
| `%p`, `%level` | Log level |
| `%c`, `%logger` | Logger name |
| `%C`, `%class` | Caller class name |
| `%M`, `%method` | Caller method name |
| `%l`, `%line` | Caller line number |
| `%L`, `%caller` | Caller location (class.method(line)) |
| `%m`, `%msg`, `%message` | Log message |
| `%n` | Newline |
| `%r`, `%R`, `%thread` | Thread name |
| `%N`, `%mdc` | MDC context (all) |
| `%mdc{key}` | MDC context (single key) |

### Building

```bash
./gradlew build
```

### License

MIT License - See [LICENSE](file:///workspace/LICENSE)

---

## NCW-Logger (中文)

一个轻量级、高性能的Java日志库，功能丰富。

### 特性

- **SLF4J 风格占位符格式化** - `{}` 参数替换
- **MDC 上下文支持** - 线程局部诊断上下文（用户ID、请求ID等）
- **多种布局** - PatternLayout、JsonLayout、SimpleLayout
- **多种输出器** - ConsoleAppender（彩色输出）、FileAppender（异步写入、轮转）
- **日志配置** - Properties 文件加载
- **日志指标** - 计数统计、吞吐量、丢弃事件
- **调用者信息提取** - 自动检测类/方法/行号
- **异常格式化** - 美观的堆栈跟踪格式化
- **分层日志** - 日志继承体系

### 快速开始

#### 基本使用

```java
import cn.ncw.logger.log.NCWLogger;
import cn.ncw.logger.log.LEVEL;

NCWLogger logger = NCWLogger.getLogger("MyApp");
logger.setLevel(LEVEL.DEBUG);

logger.info("应用程序已启动");
logger.debug("用户 {} 登录", "Alice");
logger.warn("警告：内存使用率过高");
logger.error("发生错误", new RuntimeException("测试异常"));
```

#### MDC 上下文

```java
import cn.ncw.logger.log.MDC;

MDC.put("userId", "12345");
MDC.put("requestId", "req-abc-123");
logger.info("处理请求中");
MDC.clear();
```

#### JSON 输出

```java
NCWLogger jsonLogger = NCWLogger.getLogger("JsonLogger");
jsonLogger.clearAppenders();
jsonLogger.addAppender(new Appender.ConsoleAppender("json", 
    new Layout.JsonLayout(true, true, true)) {});
```

#### 文件输出

```java
Appender.FileAppender fileAppender = Appender.file("file", 
    new Layout.PatternLayout("%d %p [%t] %c - %m%n"),
    "./logs", "app_{timestamp}.log");
logger.addAppender(fileAppender);
```

### 架构

```
┌─────────────────────────────────────────────────┐
│         NCWLogger (门面)                         │
└────────────────┬────────────────────────────────┘
                 │
    ┌────────────┴────────────┐
    │                         │
┌───▼────────┐        ┌──────▼───────┐
│  输出器     │        │  指标统计     │
├────────────┤        └──────────────┘
│ Console    │
│ File       │
│ 自定义     │
└────────────┘
       │
┌──────▼───────┐
│   布局        │
├──────────────┤
│ Pattern      │
│ Json         │
│ Simple       │
└──────────────┘
```

### 配置

#### 日志级别

| 级别 | 描述 |
|------|------|
| TRACE | 最详细的调试信息 |
| DEBUG | 调试信息 |
| INFO | 一般信息 |
| WARN | 警告消息 |
| ERROR | 错误状况 |
| FATAL | 严重错误 |
| OFF | 禁用 |

#### 模式布局占位符

| 占位符 | 描述 |
|--------|------|
| `%d`, `%date` | 日期 |
| `%t`, `%time` | 时间 |
| `%p`, `%level` | 日志级别 |
| `%c`, `%logger` | Logger 名称 |
| `%C`, `%class` | 调用者类名 |
| `%M`, `%method` | 调用者方法名 |
| `%l`, `%line` | 调用者行号 |
| `%L`, `%caller` | 调用者位置（类.方法(行号)） |
| `%m`, `%msg`, `%message` | 日志消息 |
| `%n` | 换行符 |
| `%r`, `%R`, `%thread` | 线程名 |
| `%N`, `%mdc` | MDC 上下文（全部） |
| `%mdc{key}` | MDC 上下文（单个键） |

### 构建

```bash
./gradlew build
```

### 许可证

MIT License - 见 [LICENSE](file:///workspace/LICENSE)

### 项目结构

```
/workspace
├── src/
│   └── main/
│       └── java/
│           └── cn/
│               └── ncw/
│                   └── logger/
│                       └── log/
│                           ├── Appender.java           # 输出器接口
│                           ├── Formatter.java          # 格式化工具
│                           ├── LEVEL.java              # 日志级别枚举
│                           ├── Layout.java             # 布局系统
│                           ├── LogConfig.java          # 配置文件加载
│                           ├── LogEvent.java           # 日志事件模型
│                           ├── LogMetrics.java         # 指标统计
│                           ├── Logger.java             # 简单 Logger
│                           ├── MDC.java                # MDC 上下文
│                           ├── NCWLogger.java          # 主门面
│                           ├── NCWLoggerFactory.java   # 工厂类
│                           └── ThreadLogger.java       # 线程安全 Logger
├── build.gradle.kts        # Gradle 构建配置
├── LICENSE                 # MIT 许可证
└── README.md               # 本文档
```

# 多电梯调度模拟

本项目是面向对象课程 Elevator3 任务的一个实现：读取课程提供的输入请求（乘客请求、临时调度、双电梯更新/换乘设置），通过多线程模拟 6 部电梯的运行，并用 `TimableOutput` 输出事件序列。

## 运行入口与依赖

- 入口类：[MainClass.java](src/MainClass.java)
- 外部依赖：`com.oocourse.elevator3.*`（如 `ElevatorInput`、`Request`、`TimableOutput` 等）。该依赖通常由课程环境/评测提供，仓库内不包含对应 jar。
- 运行方式：
  - 在 IntelliJ IDEA 中直接运行 `MainClass.main`，并将课程提供的依赖加入项目 classpath。
  - 程序从标准输入读取请求，直到读到 `null`（输入结束）后逐步收尾退出。

## 总体架构

程序由 3 类核心线程协作完成：

- **输入线程**：[InputThread.java](src/InputThread.java)
  - 使用 `ElevatorInput(System.in)` 读取输入 `Request`
  - 将请求转换为内部对象并放入全局请求队列：
    - `PersonRequest` -> [Person.java](src/Person.java)
    - `ScheRequest` -> [TempSchedule.java](src/TempSchedule.java)
    - `UpdateRequest` -> [Update.java](src/Update.java)

- **调度线程**：[ScheduleThread.java](src/ScheduleThread.java)
  - 从全局 [RequestQueue.java](src/RequestQueue.java) 取出请求进行分发
  - 乘客请求通过 [Dispatcher.java](src/Dispatcher.java) 选择电梯后，投入该电梯的 [WaitQueue.java](src/WaitQueue.java)
  - 临时调度请求投递到对应电梯的 `tempSchedules`（每部电梯一份）
  - 更新请求创建 [UpdateThread.java](src/UpdateThread.java) 协调两部电梯完成 UPDATE 流程

- **电梯线程（6 个）**：[ElevatorThread.java](src/ElevatorThread.java)
  - 每部电梯维护自身状态（楼层/方向/开关门/是否调度/是否更新等）与乘客集合
  - 通过策略接口 [Strategy.java](src/Strategy.java) 获取下一步状态，当前实现为 [LookStrategy.java](src/LookStrategy.java)
  - 核心动作包括：移动、开关门、进出乘客、临时调度执行、更新/换乘处理等

## 请求与队列模型

- **全局请求队列**：[RequestQueue.java](src/RequestQueue.java)
  - 存放三类内部请求对象（`Person`/`TempSchedule`/`Update`），通过 `synchronized` + `wait/notifyAll` 做线程间协作

- **电梯等待队列**：[WaitQueue.java](src/WaitQueue.java)
  - 分为上行/下行两个队列，进梯时按优先级（`priority`）从高到低取人
  - 只在当前楼层、且与电梯方向一致时允许上人

## 调度策略（选梯）

调度线程默认使用 [GradeDispatcher.java](src/GradeDispatcher.java)：

- 对每部可用电梯计算一个“得分”，选择得分最高者
- 得分由电梯对该乘客的估计耗时近似得到（见 [ElevatorThread.getScore()](src/ElevatorThread.java)）
- 若所有电梯均不可用则返回 `-1`，调度线程会等待 `sharedLock` 被唤醒后重试

另外提供了一个更简单的策略 [JudgeDispatcher.java](src/JudgeDispatcher.java)，按当前负载（人数/等待数）选择最小者；可在 [ScheduleThread.java](src/ScheduleThread.java) 中切换使用。

## Update / 换乘机制

更新请求由 [UpdateThread.java](src/UpdateThread.java) 协调两部电梯：

- 先触发两部电梯进入更新准备态，清空/转移当前队列中的乘客（需要继续乘坐的乘客会被重新投递回全局请求队列）
- 输出 `UPDATE-BEGIN`，等待一段时间后输出 `UPDATE-END`
- 为两部电梯设置同一个 [TransferFloor.java](src/TransferFloor.java)（换乘层）并将电梯区分为 A/B 两类
  - A/B 在换乘层进行“交接”与方向反转（策略中的 `TRANSFER` 分支）
  - `TransferFloor` 内部用状态与 `wait/notifyAll` 保证同一时刻只有一部电梯占用换乘层

## 输出事件

项目使用 `TimableOutput.println(...)` 输出事件，关键事件包括：

- `RECEIVE-<personId>-<elevatorId>`
- `ARRIVE-<floor>-<elevatorId>`
- `OPEN/CLOSE-<floor>-<elevatorId>`
- `IN-<personId>-<floor>-<elevatorId>`
- `OUT-S/OUT-F-<personId>-<floor>-<elevatorId>`（到达终点/中途换乘下客）
- `SCHE-BEGIN/SCHE-END-<elevatorId>`
- `UPDATE-BEGIN/UPDATE-END-<elevatorAId>-<elevatorBId>`

## 目录结构

```
.
├─ src/                 # Java 源码（默认包）
├─ out/                 # IDEA 编译/导出产物（可忽略）
├─ Homework_7.puml      # 设计图（PlantUML）
└─ UML.puml             # 设计图（PlantUML）
```


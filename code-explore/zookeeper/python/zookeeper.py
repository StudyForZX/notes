import config
import os, json
from kazoo.exceptions import LockTimeout
from kazoo.client import KazooState, KazooClient

class Zookeeper:

    # zk instance
    instance = None

    lock = None

    def __init__(self):
        '''
            construct funtion
        '''
        self.get_connection()


    def get_instance(self):
        '''
            获取单例
        '''
        if None == self.instance:

            print('instace has been expired, connect again')

            return self.get_connection()

        return self.instance


    def get_connection(self):
        '''
            connect zookeeper and return the instance
        '''
        self.instance = KazooClient(
            hosts=config.zk_hosts,
            timeout=config.zk_client_timeout,
        )
        # 启动这里会自己判断如果连接在默认时间超时，抛出异常
        self.instance.start()

        self.instance.add_listener(self.connection_listener)

        print('client id is: %d, password is: %s ' % (self.instance.client_id[0], self.instance.client_id[1]))

        print('state is %s' % self.instance.state)

        print('last state is %s' % self.instance.state)

        return self.instance


    def connection_listener(self, state):
        '''
        Add a function to be called for connection state changes.

        This function will be called with a KazooState instance indicating the new connection state on state transitions.
        '''
        if state == KazooState.LOST:
            # Register somewhere that the session was lost
            print('current state is lost')
            pass
        elif state == KazooState.SUSPENDED:
            # Handle being disconnected from Zookeeper
            print('current state is suspended')
            pass
        else:
            # Handle being connected/reconnected to Zookeeper
            print('current state is unknow: %s' % state)
            pass


    def read_and_lock(self, path):
        '''
            add a exclusive read lock
        '''
        zk = self.get_instance()

        node_info = zk

        lock = zk.Lock(path, identifier="this is a test")

        # do someting with lock
        return lock, node_info


    def read_node_path_recursively(self, root_path,  deep = 2, need_paths = []):
        '''
            递归获取目录
        '''
        parent_paths = self.instance.get_children(root_path)

        for child_path in parent_paths:

            child_path = os.path.join(root_path, child_path)

            if deep > 0:
                need_paths = self.read_node_path_recursively(child_path, deep - 1, need_paths)
            else:
                need_paths.append(child_path)

        return need_paths


    def get_task_from_zookeeper(self, root_path):
        '''
            从zk获取一个任务
        '''
        # 初始化 防止没有任务列表返回报错
        task = ''

        for task_path in self.read_node_path_recursively(root_path):

            print('读取当前任务节点: %s' % task_path)

            self.task_path = task_path

            # Returns: Tuple (value, ZnodeStat) of node.
            task = self.instance.get(task_path)[0]

            task = json.loads(task)

            # 如果任务已经被处理过，则跳过
            if 1 == task['is_handle']:
                task = ''
                continue

            # 判断是否已经加锁，如果加锁了
            self.lock = self.instance.Lock(task_path, 'lock this node in case recovery repeatedly')

            print('任务加锁中...')

            try:
                # 如果超时 则读取下一
                self.lock.acquire(timeout = 5.0)
            except LockTimeout as e:
                print('加锁失败...')
                self.lock.release()
                continue

            print('加锁成功...')

            break

        return task

    def delete_task_from_zookeeper(self, task):
        '''
            从zk中删除一个已经完成的任务
            1. 删除任务的第一步是将节点的值添加个字段 is_handle = 1 不能直接释放锁
            2. 释放锁
            3. 删除任务
        '''
        # 更改任务的值
        task['is_handle'] = 1
        # 需要转换为二进制编码
        task = bytes(json.dumps(task), encoding='UTF-8')

        self.instance.set(self.task_path, task)

        # 释放锁
        self.lock.release()
        # 删除当前任务
        # self.instance.delete(self.task_path)

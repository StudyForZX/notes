import time, threading
from zookeeper import Zookeeper
from kazoo.exceptions import LockTimeout

def mock_data(zk):
    '''
        造一些数据
    '''

    root_path = '/hzx'

    zk.get_instance().create('/hzx', bytes('0', encoding="utf8"))
    zk.get_instance().create('/hzx/c1', bytes('0', encoding="utf8"))
    zk.get_instance().create('/hzx/c1/task1', bytes('0', encoding="utf8"))
    zk.get_instance().create('/hzx/c1/task1/2022.01.01.01', bytes('{"is_handle": 0}', encoding="utf8"))
    zk.get_instance().create('/hzx/c1/task1/2022.01.01.02', bytes('{"is_handle": 0}', encoding="utf8"))
    zk.get_instance().create('/hzx/c1/task1/2022.01.01.03', bytes('{"is_handle": 0}', encoding="utf8"))
    zk.get_instance().create('/hzx/c1/task1/2022.01.01.04', bytes('{"is_handle": 0}', encoding="utf8"))


def index():
    '''
        the program entrance
    '''
    zk = Zookeeper()

    # mock_data(zk)
    # return

    task = zk.get_task_from_zookeeper('hzx')

    lock, node_info = zkObj.read_and_lock(path)

    try:
        lock.acquire(timeout=10)
        print('acquire the lock sucessfuly')
    except LockTimeout:
        print('acquire the lock timeout')
        return

    print(node_info)

    time.sleep(300)

    zk.delete_task_from_zookeeper(task)

    zk.instance.stop()


index()


# t = threading.Timer(30, index)
# t.start()

# t1 = threading.Timer(30, index)
# t1.start()

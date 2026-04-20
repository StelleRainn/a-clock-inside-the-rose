// IndexedDB 数据库配置
// 在现代浏览器中，相比于容量极小（5MB）且同步阻塞的 localStorage，
// IndexedDB 提供了大容量、异步非阻塞的本地数据库解决方案，特别适合存储图片 Blob、音频文件等大型二进制数据。
const DB_NAME = 'acir_db';
const STORE_NAME = 'images'; // 类似于关系型数据库中的“表名”
const DB_VERSION = 1;

export const imageDb = {
  /**
   * 获取/初始化数据库连接
   * 使用 Promise 封装原生的基于事件的 IndexedDB API，使其支持 async/await 的现代异步写法
   */
  async getDb() {
    return new Promise((resolve, reject) => {
      // 尝试打开数据库。如果数据库不存在，或者版本号升高，会触发 onupgradeneeded 事件
      const request = indexedDB.open(DB_NAME, DB_VERSION);

      request.onerror = () => reject(request.error);
      request.onsuccess = () => resolve(request.result);

      // 数据库初始化或版本升级时的钩子：这是唯一可以创建或删除 ObjectStore 的地方
      request.onupgradeneeded = (event) => {
        const db = event.target.result;
        // 检查是否已经存在名为 'images' 的存储空间（表）
        if (!db.objectStoreNames.contains(STORE_NAME)) {
          // 创建一个键值对存储空间。此处没有指定 keyPath，意味着插入数据时需要手动传入键名（例如用户ID）
          db.createObjectStore(STORE_NAME);
        }
      };
    });
  },

  /**
   * 将图片存入 IndexedDB
   * @param {string} id - 图片的唯一标识符（例如 userId）
   * @param {Blob} blob - 图片的二进制数据对象
   */
  async saveImage(id, blob) {
    const db = await this.getDb();
    return new Promise((resolve, reject) => {
      // 开启一个读写事务 (readwrite)
      const transaction = db.transaction([STORE_NAME], 'readwrite');
      const store = transaction.objectStore(STORE_NAME);
      
      // 使用 put 方法：如果 ID 存在则覆盖更新，不存在则新增
      // 这是“乐观”存储策略，避免了先查后写的竞态问题
      const request = store.put(blob, id);

      request.onsuccess = () => resolve();
      request.onerror = () => reject(request.error);
    });
  },

  /**
   * 从 IndexedDB 读取图片
   * @param {string} id - 图片的唯一标识符
   * @returns {Promise<Blob>} 返回图片的二进制 Blob 对象
   */
  async getImage(id) {
    const db = await this.getDb();
    return new Promise((resolve, reject) => {
      // 开启一个只读事务 (readonly)，只读事务在浏览器底层可以并发执行，性能更好
      const transaction = db.transaction([STORE_NAME], 'readonly');
      const store = transaction.objectStore(STORE_NAME);
      const request = store.get(id);

      request.onsuccess = () => resolve(request.result);
      request.onerror = () => reject(request.error);
    });
  },

  /**
   * 从 IndexedDB 删除指定图片
   * @param {string} id - 图片的唯一标识符
   */
  async deleteImage(id) {
    const db = await this.getDb();
    return new Promise((resolve, reject) => {
      const transaction = db.transaction([STORE_NAME], 'readwrite');
      const store = transaction.objectStore(STORE_NAME);
      const request = store.delete(id);

      request.onsuccess = () => resolve();
      request.onerror = () => reject(request.error);
    });
  }
};


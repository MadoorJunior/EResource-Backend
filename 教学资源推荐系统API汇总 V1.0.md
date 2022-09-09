# 教学资源推荐系统API汇总 V1

## 文档说明

### 服务器API地址

前缀：`http://服务器ip:9000/e-resource/api`

完整的API地址为：`前缀` + `具体接口路径`

### 调用接口说明

+ 关于用户认证和权限管理

  + 当接口中**包含**`public`的，不需要登录和相应权限
  + 当接口中**包含**`private`的，需要登录和相应权限
  
+ 当token无效或者已过期时，返回：

  ```json
  {
      "code": 401,
      "message": "token无效或已过期"
  }
  ```

+ 所有的接口的返回形式都是统一为：

  + 正常返回

    ```json
    {
        "code": 200,
        "message": "OK",
        "data": "某种类型的数据，比如字符串，数字，对象（字典）等等"
    }
    ```

  + 错误返回

    ```json
    {
        "code": "具体的错误码",
        "message": "具体的错误信息字符串"
    }
    ```

## 账号操作

### 创建新用户

#### Request

+ Method: **POST**

+ URL: `/v1.0/open/register`

+ Headers: `Content-Type: application/json`

+ Body:

  ```json
  {
      "username": "ZhangSan",
      "password": "123456Abc@",
      "email": "zhangsan@njnu.edu.com"
  }
  ```

#### Response

+ Body

  ```json
  {
      "code": 200,
      "message": "创建成功",
      "data": {
          "userID": 1,
          "userType": 1
      }
  }
  ```

### 登录

#### Request

+ Method: **POST**

+ URL: `/v1.0/open/login`

+ Headers: `Content-Type: application/json`

+ Body:

  ```json
  {
      "username": "ZhangSan",
      "password": "123456Abc@"
  }
  ```

#### Response

+ Body

  ```json
  {
      "code": 200,
      "message": "登录成功",
      "data": {
          "userID": 1,
          "userType": 1
      }
  }
  ```

### 修改个人信息

#### Request

+ Method: **PATCH**

+ URL: `/v1.0/user/{username}`

+ Headers: `Content-Type: application/json`

+ Body:

  ```json
  {
      "password": "123456Abc@",
      "email": "ZhangSan@njnu.edu.com"
  }
  ```

#### Response

+ Body

  ```json
  {
      "code": 200,
      "message": "修改成功",
      "data": null
  }
  ```

## 普通用户操作

### 获取资源列表

#### Request

+ Method: **GET**
+ URL: `/v1.0/open/resources?page={page}&perPage={perPage}`
  + `page`代表查询第几页
  + `perPage`代表每一页资源的个数

#### Response

+ Body

  ```json
  {
      "code": 200,
      "message": "查询成功",
      "data": {
          "page": 1,
          "perPage": 10,
          "pages": 1000,
          "total": 10000,
          "resources": [
              {
                  "resourceID": 1,
                  "resourceType": 5, 
                  "name": "静夜思",
                  "gradeSubject": 2,
                  "download": 21,
                  "resource": "...",
                  "collection": 97,
                  "url": "http://...",
                  "entity": ["李白", "..."]
              },
              {
                "...": "..."
              } 
          ]
      }
  }
  ```

### 根据条件查询资源

#### Request

+ Method: **GET**
+ URL: `/v1.0/open/resources/search/{keyword}?resourceType={resourceType}&gradeSubject={gradeSubject}&updateTime={updateTime}&relevantType={relevantType}&relevantID={relevantID}&page={page}&perPage={perPage} `
  + `keyword`代表关键词
  + `resourceType`代表资源类型
  + `gradeSubject`代表年段
  + `updateTime`代表更新时间
  + `relevantType`代表相关联的资源类型
  + `relevantID`代表相关联的资源ID
  + `page`代表查询第几页
  + `perPage`代表每一页资源的个数

#### Response

+ Body

  ```json
  {
      "code": 200,
      "message": "查询成功",
      "data": {
          "page": 1,
          "perPage": 10,
          "pages": 2,
          "total": 15,
          "resources": [
              {
                  "resourceID": 1,
                  "gradeSubject": 2,
                  "name": "静夜思",
                  "download": 21,
                  "resource": "...",
                  "collection": 97,
                  "url": "http://...",
                  "entity": ["李白", "..."]
              },
              {
                  "...": "..."
              } 
          ]
      }
  }
  ```

### 查看具体的资源

#### Request

+ Method: **GET**
+ URL: `/v1.0/open/resources/{resourceType}/{resourceID} `
  + `resourceType`代表资源类型
  + `resourceID`代表资源ID
+ Headers: `userInfo: { username: "Zhangsan" }`

#### Response

* Body

  ```json
  {
      "code": 200,
      "message": "查询成功",
      "data": {
          "resourceID": 1,
          "gradeSubject": 2,
          "name": "静夜思",
          "download": 21,
          "resource": "...",
          "collection": 97,
          "url": "http://...",
          "entity": ["李白", "..."]
      }
  }
  ```

### 收藏资源

#### Request

+ Method: **POST**
+ URL: `v1.0/favorites/{resourceType}/{resourceID}`
  + `resourceType`代表资源类型
  + `resourceID`代表资源ID
+ Headers: `userInfo: { username: "Zhangsan" }`

#### Response

+ Body

  ```json
  {
      "code": 200,
      "message": "收藏成功",
      "data": null
  }
  ```

### 查看收藏

#### Request

+ Method: **GET**
+ URL: `v1.0/favorites/`
+ Headers: `userInfo: { username: "Zhangsan" }`

#### Response

+ Body

  ```json
  {
      "code": 200,
      "message": "收藏成功",
      "data": [
          {
              "resourceID": 1,
              "gradeSubject": 2,
              "name": "静夜思",
              "download": 21,
              "resource": "...",
              "collection": 97,
              "url": "http://...",
              "entity": ["李白", "..."]
          },
          {
              "...":  "..."
          }
      ]
  }
  ```

### 下载资源

#### Request

+ Method: **GET**
+ URL: `/v1.0/download/{resourceType}/{resourceID} `
  + `resourceType`代表资源类型
  + `resourceID`代表资源ID
+ Headers: `userInfo: { username: "Zhangsan" }`

#### Response

* 文件

### 查看浏览记录

#### Request

+ Method: **GET**
+ URL: `/v1.0/history/{page}/{perPage}`
  + `page`代表查询第几页
  + `perPage`代表每一页资源的个数
+ Headers: `userInfo: { username: "Zhangsan" }`

#### Response

* Body

  ```json
  {
      "code": 200,
      "message": "查询成功",
      "data": [
          {
              "resourceID": 1,
              "gradeSubject": 2,
              "name": "静夜思",
              "download": 21,
              "resource": "...",
              "collection": 97,
              "url": "http://...",
              "entity": ["李白", "..."],
     			"date": 1611837233181
          },
          {
              "...": "..."
          }
      ]
  }
  ```

### 记录浏览时长

#### Request

+ Method: **POST**

+ URL: `/v1.0/records/{resourceType}/{resourceID}`

  + `resourceType`代表资源类型
  + `resourceID`代表资源ID

+ Headers: `Content-Type: application/json`

+ Body:

  ```json
  {
      "username": "ZhangSan",
      "duration": 100
  }
  ```

#### Response

* Body

  ```json
  {
      "code": 200,
      "message": "记录成功",
      "data": null
  }
  ```

### 查看智能学习路径

#### Request

+ Method: **GET**
+ URL: `/v1.0/path`
+ Headers: `userInfo: { username: "Zhangsan" }`

#### Response

* Body

  ```json
  {
      "code": 200,
      "message": "查询成功",
      "data": [
          {
              "entity": "..."
          },
          {
              "...": "..."
          }
      ]
  }
  ```

### 查看教案切片

#### Request

+ Method: **GET**
+ URL: `/v1.0/plans/{entity}`
  + `entity`代表知识点
+ Headers: `userInfo: { username: "Zhangsan" }`

#### Response

* Body

  ```json
  {
      "code": 200,
      "message": "生成成功",
      "data": [
          {
              "planID": "1",
              "content": "教案切片内容"
          }
      ]
  }
  ```

### 查看测试题

#### Request

+ Method: **GET**
+ URL: `/v1.0/quiestions/{entity}`
  + `entity`代表知识点
+ Headers: `userInfo: { username: "Zhangsan" }`

#### Response

* Body

  ```json
  {
      "code": 200,
      "message": "生成成功",
      "data": [
          {
              "paperID": "1",
              "content": "题目内容"
          }
      ]
  }
  ```

### 获取推荐资源

#### Request

+ Method: **GET**
+ URL: `/v1.0/recommendations/`
+ Headers: `userInfo: { username: "Zhangsan" }`

#### Response

* Body

  ```json
  {
      "code": 200,
      "message": "生成成功",
      "data": [
          {
              "resourceID": 1,
              "name": "静夜思",
              "gradeSubject": 3,
              "download": 97
          }
      ]
  }
  ```

## 管理员操作

### 添加资源

#### Request

+ Method: **PUT**

+ URL: `/v1.0/resources/`

+ Headers: `Content-Type: application/json`

+ Body: 

  ```json
  {
      "resourceType": 5,
      "name": "静夜思",
      "gradeSubject": 2,
      "content": "资源内容"
  }
  ```

#### Response

* Body

  ```json
  {
      "code": 200,
      "message": "添加成功",
      "data": null
  }
  ```

**注：**因添加资源的操作可能涉及到文件上传以及服务器后续的处理和相应的IO操作，因此该的操作应分离出来另起一个服务

### 删除资源

#### Request

+ Method: **DELETE**

+ URL: `/v1.0/resources/`

+ Headers: `Content-Type: application/json`

+ Body: 

  ```json
  {
      "resourceType": 5, 
      "resourceID": 1
  }
  ```

#### Response

+ Body

  ```json
  {
      "code": 200,
      "message": "删除成功 || 内容已被删除",
      "data": {
  		"deletedCount": "1 || 0"
  	}
  }
  ```

### 修改资源

#### Request

+ Method: **PATCH**

+ URL: `/v1.0/resources/`

+ Headers: `Content-Type: application/json`

+ Body: 

  ```json
  {
      "resourceType": 5,
      "resourceID": 1,
      "name": "静夜思",
      "gradeSubject": 2,
      "content": "资源内容"
  }
  ```

#### Response

+ Body

  ```json
  {
      "code": 200,
      "message": "修改成功",
      "data": null
  }
  ```

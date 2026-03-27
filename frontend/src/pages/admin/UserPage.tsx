// 后台用户管理页面
import React, { useState, useEffect } from 'react';
import { Table, Card, Button, Space, Input, message, Popconfirm, Avatar } from 'antd';
import { SearchOutlined, DeleteOutlined, UserOutlined } from '@ant-design/icons';
import { getUserList, deleteUser } from '@services/user';
import type { User } from '@types';

const AdminUserPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [users, setUsers] = useState<User[]>([]);
  const [keyword, setKeyword] = useState('');

  const loadUsers = async () => {
    setLoading(true);
    try {
      const params: any = { page: 1, pageSize: 100 };
      if (keyword) params.keyword = keyword;
      const data = await getUserList(params);
      setUsers(data.data?.list || []);
    } catch (error) {
      console.error('加载用户失败:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadUsers();
  }, []);

  const handleDelete = async (id: string) => {
    try {
      await deleteUser(id);
      message.success('删除成功');
      loadUsers();
    } catch (error) {
      message.error('删除失败');
    }
  };

  const columns = [
    {
      title: '头像',
      dataIndex: 'avatar',
      key: 'avatar',
      render: (avatar: string, record: User) => (
        <Avatar src={avatar} icon={<UserOutlined />}>
          {record.username[0]?.toUpperCase()}
        </Avatar>
      ),
    },
    {
      title: '用户名',
      dataIndex: 'username',
      key: 'username',
    },
    {
      title: '邮箱',
      dataIndex: 'email',
      key: 'email',
    },
    {
      title: '手机号',
      dataIndex: 'phone',
      key: 'phone',
      render: (phone: string) => phone || '-',
    },
    {
      title: '注册时间',
      dataIndex: 'createTime',
      key: 'createTime',
      render: (time: string) => new Date(time).toLocaleString(),
    },
    {
      title: '操作',
      key: 'action',
      render: (_: any, record: User) => (
        <Space>
          <Popconfirm
            title="确定删除此用户？"
            onConfirm={() => handleDelete(record.id)}
            okText="确定"
            cancelText="取消"
          >
            <Button type="link" danger icon={<DeleteOutlined />}>删除</Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <div>
      <h2 style={{ marginBottom: 24 }}>用户管理</h2>

      <Card style={{ marginBottom: 16 }}>
        <Space>
          <Input
            placeholder="搜索用户名或邮箱"
            prefix={<SearchOutlined />}
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            onPressEnter={loadUsers}
            style={{ width: 200 }}
            allowClear
          />
          <Button type="primary" onClick={loadUsers}>搜索</Button>
        </Space>
      </Card>

      <Card>
        <Table
          columns={columns}
          dataSource={users}
          rowKey="id"
          loading={loading}
          pagination={{ pageSize: 10 }}
        />
      </Card>
    </div>
  );
};

export default AdminUserPage;

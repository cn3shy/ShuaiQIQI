// 后台内容管理页面
import React, { useState, useEffect } from 'react';
import { Table, Card, Button, Space, Tag, Input, Select, message, Popconfirm } from 'antd';
import { SearchOutlined, DeleteOutlined, EyeOutlined } from '@ant-design/icons';
import { getContentList, deleteContent } from '@services/content';
import { Link } from 'react-router-dom';
import type { Content } from '@types';

const AdminContentPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [contents, setContents] = useState<Content[]>([]);
  const [keyword, setKeyword] = useState('');
  const [status, setStatus] = useState<number>();

  const loadContents = async () => {
    setLoading(true);
    try {
      const params: any = { page: 1, pageSize: 100 };
      if (keyword) params.keyword = keyword;
      const data = await getContentList(params);
      setContents(data.data?.list || []);
    } catch (error) {
      console.error('加载内容失败:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadContents();
  }, []);

  const handleDelete = async (id: string) => {
    try {
      await deleteContent(id);
      message.success('删除成功');
      loadContents();
    } catch (error) {
      message.error('删除失败');
    }
  };

  const columns = [
    {
      title: '标题',
      dataIndex: 'title',
      key: 'title',
      render: (text: string, record: Content) => (
        <Link to={`/content/${record.id}`}>{text}</Link>
      ),
    },
    {
      title: '作者',
      dataIndex: ['author', 'username'],
      key: 'author',
    },
    {
      title: '分类',
      dataIndex: 'categoryId',
      key: 'categoryId',
      render: (categoryId: string) => categoryId ? <Tag color="blue">{categoryId}</Tag> : '-',
    },
    {
      title: '点赞数',
      dataIndex: 'likeCount',
      key: 'likeCount',
      sorter: (a: Content, b: Content) => a.likeCount - b.likeCount,
    },
    {
      title: '评论数',
      dataIndex: 'commentCount',
      key: 'commentCount',
      sorter: (a: Content, b: Content) => a.commentCount - b.commentCount,
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
      render: (time: string) => new Date(time).toLocaleString(),
    },
    {
      title: '操作',
      key: 'action',
      render: (_: any, record: Content) => (
        <Space>
          <Link to={`/content/${record.id}`}>
            <Button type="link" icon={<EyeOutlined />}>查看</Button>
          </Link>
          <Popconfirm
            title="确定删除此内容？"
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
      <h2 style={{ marginBottom: 24 }}>内容管理</h2>

      <Card style={{ marginBottom: 16 }}>
        <Space>
          <Input
            placeholder="搜索内容标题"
            prefix={<SearchOutlined />}
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            onPressEnter={loadContents}
            style={{ width: 200 }}
            allowClear
          />
          <Button type="primary" onClick={loadContents}>搜索</Button>
        </Space>
      </Card>

      <Card>
        <Table
          columns={columns}
          dataSource={contents}
          rowKey="id"
          loading={loading}
          pagination={{ pageSize: 10 }}
        />
      </Card>
    </div>
  );
};

export default AdminContentPage;

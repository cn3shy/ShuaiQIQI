import React, { useState, useEffect } from 'react';
import { Card, Avatar, Button, Space, Form, Input, message } from 'antd';
import { LikeOutlined, CommentOutlined, StarOutlined, ArrowLeftOutlined } from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import { getContentDetail, likeContent, favoriteContent } from '@services/content';
import { createComment } from '@services/comment';
import type { Content, Comment } from '@types';

/**
 * 内容详情页
 */
const ContentDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [content, setContent] = useState<Content | null>(null);
  const [comments, setComments] = useState<Comment[]>([]);
  const [commentLoading, setCommentLoading] = useState(false);

  const loadContent = async () => {
    if (!id) return;
    setLoading(true);
    try {
      const response = await getContentDetail(id);
      setContent(response.data);
    } catch (error) {
      console.error('加载内容失败:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadContent();
  }, [id]);

  const handleLike = async () => {
    if (!content?.id) return;
    try {
      await likeContent(content.id);
      setContent({
        ...content!,
        isLiked: !content!.isLiked,
        likeCount: content!.isLiked ? content!.likeCount - 1 : content!.likeCount + 1,
      });
    } catch (error) {
      message.error('操作失败');
    }
  };

  const handleFavorite = async () => {
    if (!content?.id) return;
    try {
      await favoriteContent(content.id);
      setContent({
        ...content!,
        isFavorited: !content!.isFavorited,
        favoriteCount: content!.isFavorited ? content!.favoriteCount - 1 : content!.favoriteCount + 1,
      });
    } catch (error) {
      message.error('操作失败');
    }
  };

  const handleComment = async (values: any) => {
    if (!content?.id) return;
    setCommentLoading(true);
    try {
      const response = await createComment({
        contentId: content.id,
        content: values.content,
      });
      setComments([response.data, ...comments]);
      message.success('评论成功');
    } catch (error) {
      message.error('评论失败');
    } finally {
      setCommentLoading(false);
    }
  };

  if (loading) {
    return (
      <div style={{ padding: 24, textAlign: 'center' }}>
        <Space size="large">
          <div>加载中...</div>
        </Space>
      </div>
    );
  }

  if (!content) {
    return (
      <div style={{ padding: 24, textAlign: 'center' }}>
        内容不存在
      </div>
    );
  }

  return (
    <div style={{ maxWidth: 1000, margin: '0 auto', padding: 24 }}>
      <Button
        type="text"
        icon={<ArrowLeftOutlined />}
        onClick={() => navigate(-1)}
        style={{ marginBottom: 16 }}
      >
        返回
      </Button>

      <Card style={{ marginBottom: 16, borderRadius: 8 }}>
        <h1 style={{ marginBottom: 16, fontSize: 24, color: '#333' }}>{content.title}</h1>

        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24, paddingBottom: 24, borderBottom: '1px solid #f0f0f0' }}>
          <Space size={16}>
            <Avatar src={content.author.avatar} size={40} style={{ backgroundColor: '#1890ff' }}>
              {content.author.username[0]?.toUpperCase()}
            </Avatar>
            <div>
              <div style={{ fontSize: 16, fontWeight: 500 }}>{content.author.username}</div>
              <div style={{ fontSize: 12, color: '#999' }}>
                {new Date(content.createTime).toLocaleString()}
              </div>
            </div>
          </Space>

          <Space size={24}>
            <div style={{ textAlign: 'center' }}>
              <LikeOutlined style={{ fontSize: 18, color: content.isLiked ? '#1890ff' : '#666' }} />
              <div style={{ fontSize: 12, color: '#999' }}>{content.likeCount}</div>
            </div>
            <div style={{ textAlign: 'center' }}>
              <CommentOutlined style={{ fontSize: 18, color: '#666' }} />
              <div style={{ fontSize: 12, color: '#999' }}>{content.commentCount}</div>
            </div>
            <div style={{ textAlign: 'center' }}>
              <StarOutlined style={{ fontSize: 18, color: content.isFavorited ? '#faad14' : '#666' }} />
              <div style={{ fontSize: 12, color: '#999' }}>{content.favoriteCount}</div>
            </div>
          </Space>
        </div>

        {content.coverImage && (
          <img
            src={content.coverImage}
            alt={content.title}
            style={{ width: '100%', marginBottom: 24, borderRadius: 4 }}
          />
        )}

        <div
          style={{
            padding: '24px 0',
            fontSize: 16,
            lineHeight: 1.8,
            color: '#333',
          }}
        >
          {content.content}
        </div>
      </Card>

      <Card style={{ marginBottom: 16, borderRadius: 8 }}>
        <Space>
          <Button onClick={handleLike} icon={content.isLiked ? <LikeOutlined /> : <LikeOutlined />}>
            {content.isLiked ? '已赞' : '点赞'}
          </Button>
          <Button onClick={handleFavorite} icon={content.isFavorited ? <StarOutlined /> : <StarOutlined />}>
            {content.isFavorited ? '已收藏' : '收藏'}
          </Button>
        </Space>
      </Card>

      <Card title={`评论 (${comments.length})`} style={{ borderRadius: 8 }}>
        <Form layout="vertical" onFinish={handleComment} style={{ marginBottom: 16 }}>
          <Form.Item name="content" rules={[{ required: true, message: '请输入评论内容' }]}>
            <Input.TextArea
              placeholder="写下你的评论..."
              rows={3}
              allowClear
            />
          </Form.Item>
          <Form.Item>
            <Button type="primary" htmlType="submit" loading={commentLoading}>
              发布评论
            </Button>
          </Form.Item>
        </Form>

        {comments.length === 0 ? (
          <div style={{ textAlign: 'center', padding: 24, color: '#999' }}>
            暂无评论，来发表第一条评论吧！
          </div>
        ) : (
          comments.map((comment) => (
            <div key={comment.id} style={{ marginBottom: 16, paddingBottom: 16, borderBottom: '1px solid #f0f0f0' }}>
              <div style={{ display: 'flex', alignItems: 'flex-start' }}>
                <Avatar src={comment.user?.avatar} style={{ backgroundColor: '#1890ff', marginRight: 12 }}>
                  {comment.user?.username[0]?.toUpperCase()}
                </Avatar>
                <div style={{ flex: 1 }}>
                  <div style={{ marginBottom: 4 }}>
                    <strong>{comment.user?.username}</strong>
                    <span style={{ marginLeft: 12, fontSize: 12, color: '#999' }}>
                      {new Date(comment.createTime).toLocaleString()}
                    </span>
                  </div>
                  <p style={{ marginBottom: 8, color: '#666' }}>{comment.content}</p>
                  <Space size={12}>
                    <Button type="link" size="small" onClick={() => console.log('回复')}>回复</Button>
                    <Button type="link" size="small" onClick={() => console.log('点赞')}>点赞 ({comment.likeCount})</Button>
                  </Space>
                </div>
              </div>
            </div>
          ))
        )}
      </Card>
    </div>
  );
};

export default ContentDetailPage;
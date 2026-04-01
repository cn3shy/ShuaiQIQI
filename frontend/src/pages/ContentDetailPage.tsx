import React, { useState, useEffect } from 'react';
import { Card, Avatar, Button, Space, Form, Input, message, Spin, Empty } from 'antd';
import { LikeOutlined, LikeFilled, StarOutlined, StarFilled, ArrowLeftOutlined } from '@ant-design/icons';
import { useNavigate, useParams } from 'react-router-dom';
import {
  getContentDetail,
  likeContent,
  unlikeContent,
  favoriteContent,
  unfavoriteContent,
} from '@services/content';
import { getCommentList, createComment, likeComment, unlikeComment } from '@services/comment';
import { useAuthStore } from '@stores/auth';
import type { Content, Comment } from '@types';

/**
 * 内容详情页
 */
const ContentDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { user } = useAuthStore();
  const [loading, setLoading] = useState(false);
  const [content, setContent] = useState<Content | null>(null);
  const [comments, setComments] = useState<Comment[]>([]);
  const [commentTotal, setCommentTotal] = useState(0);
  const [commentLoading, setCommentLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  const loadContent = async () => {
    if (!id) return;
    setLoading(true);
    try {
      const response = await getContentDetail(id);
      setContent(response.data);
    } catch (error) {
      console.error('加载内容失败:', error);
      message.error('加载内容失败');
    } finally {
      setLoading(false);
    }
  };

  const loadComments = async () => {
    if (!id) return;
    setCommentLoading(true);
    try {
      const response = await getCommentList(id, { page: 1, pageSize: 50 });
      setComments(response.data?.list || []);
      setCommentTotal(response.data?.total || 0);
    } catch (error) {
      console.error('加载评论失败:', error);
    } finally {
      setCommentLoading(false);
    }
  };

  useEffect(() => {
    loadContent();
    loadComments();
  }, [id]);

  const handleLike = async () => {
    if (!content?.id) return;
    try {
      if (content.isLiked) {
        await unlikeContent(content.id);
      } else {
        await likeContent(content.id);
      }
      setContent(prev => prev ? {
        ...prev,
        isLiked: !prev.isLiked,
        likeCount: prev.isLiked ? prev.likeCount - 1 : prev.likeCount + 1,
      } : null);
    } catch (error) {
      const err = error as Error;
      message.error(err.message || '操作失败');
    }
  };

  const handleFavorite = async () => {
    if (!content?.id) return;
    try {
      if (content.isFavorited) {
        await unfavoriteContent(content.id);
      } else {
        await favoriteContent(content.id);
      }
      setContent(prev => prev ? {
        ...prev,
        isFavorited: !prev.isFavorited,
        favoriteCount: prev.isFavorited ? prev.favoriteCount - 1 : prev.favoriteCount + 1,
      } : null);
    } catch (error) {
      const err = error as Error;
      message.error(err.message || '操作失败');
    }
  };

  const handleComment = async (values: { content: string }) => {
    if (!content?.id || !user) {
      message.warning('请先登录');
      return;
    }
    setSubmitting(true);
    try {
      const response = await createComment({
        contentId: content.id,
        content: values.content,
      });
      setComments(prev => [response.data, ...prev]);
      setCommentTotal(commentTotal + 1);
      setContent(prev => prev ? {
        ...prev,
        commentCount: prev.commentCount + 1,
      } : null);
      message.success('评论成功');
    } catch (error) {
      const err = error as Error;
      message.error(err.message || '评论失败');
    } finally {
      setSubmitting(false);
    }
  };

  const handleCommentLike = async (commentId: string, isLiked: boolean) => {
    if (!user) {
      message.warning('请先登录');
      return;
    }
    try {
      if (isLiked) {
        await unlikeComment(commentId);
      } else {
        await likeComment(commentId);
      }
      setComments(prev => prev.map((c) =>
        c.id === commentId
          ? {
              ...c,
              isLiked: !c.isLiked,
              likeCount: c.isLiked ? c.likeCount - 1 : c.likeCount + 1,
            }
          : c
      ));
    } catch (error) {
      const err = error as Error;
      message.error(err.message || '操作失败');
    }
  };

  if (loading) {
    return (
      <div style={{ padding: 24, textAlign: 'center' }}>
        <Spin size="large" />
      </div>
    );
  }

  if (!content) {
    return (
      <div style={{ padding: 24, textAlign: 'center' }}>
        <Empty description="内容不存在" />
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

        <div
          style={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            marginBottom: 24,
            paddingBottom: 24,
            borderBottom: '1px solid #f0f0f0',
          }}
        >
          <Space size={16}>
            <Avatar src={content.author?.avatar} size={40} style={{ backgroundColor: '#1890ff' }}>
              {content.author?.username?.[0]?.toUpperCase()}
            </Avatar>
            <div>
              <div style={{ fontSize: 16, fontWeight: 500 }}>{content.author?.username}</div>
              <div style={{ fontSize: 12, color: '#999' }}>
                {new Date(content.createTime).toLocaleString()}
              </div>
            </div>
          </Space>

          <Space size={24}>
            <div style={{ textAlign: 'center' }}>
              {content.isLiked ? (
                <LikeFilled style={{ fontSize: 18, color: '#1890ff' }} />
              ) : (
                <LikeOutlined style={{ fontSize: 18, color: '#666' }} />
              )}
              <div style={{ fontSize: 12, color: '#999' }}>{content.likeCount}</div>
            </div>
            <div style={{ textAlign: 'center' }}>
              <span style={{ fontSize: 18, color: '#666' }}>{commentTotal}</span>
              <div style={{ fontSize: 12, color: '#999' }}>评论</div>
            </div>
            <div style={{ textAlign: 'center' }}>
              {content.isFavorited ? (
                <StarFilled style={{ fontSize: 18, color: '#faad14' }} />
              ) : (
                <StarOutlined style={{ fontSize: 18, color: '#666' }} />
              )}
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
            whiteSpace: 'pre-wrap',
          }}
        >
          {content.content}
        </div>
      </Card>

      <Card style={{ marginBottom: 16, borderRadius: 8 }}>
        <Space>
          <Button
            type={content.isLiked ? 'primary' : 'default'}
            icon={content.isLiked ? <LikeFilled /> : <LikeOutlined />}
            onClick={handleLike}
          >
            {content.isLiked ? '已赞' : '点赞'}
          </Button>
          <Button
            type={content.isFavorited ? 'primary' : 'default'}
            icon={content.isFavorited ? <StarFilled /> : <StarOutlined />}
            onClick={handleFavorite}
            style={content.isFavorited ? { background: '#faad14', borderColor: '#faad14' } : {}}
          >
            {content.isFavorited ? '已收藏' : '收藏'}
          </Button>
        </Space>
      </Card>

      <Card title={`评论 (${commentTotal})`} style={{ borderRadius: 8 }}>
        {user ? (
          <Form layout="vertical" onFinish={handleComment} style={{ marginBottom: 24 }}>
            <Form.Item name="content" rules={[{ required: true, message: '请输入评论内容' }]}>
              <Input.TextArea placeholder="写下你的评论..." rows={3} allowClear />
            </Form.Item>
            <Form.Item>
              <Button type="primary" htmlType="submit" loading={submitting}>
                发布评论
              </Button>
            </Form.Item>
          </Form>
        ) : (
          <div style={{ textAlign: 'center', padding: 16, marginBottom: 24, background: '#f5f5f5', borderRadius: 8 }}>
            <span style={{ color: '#666' }}>请先登录后发表评论</span>
            <Button type="link" onClick={() => navigate('/login')}>去登录</Button>
          </div>
        )}

        <Spin spinning={commentLoading}>
          {comments.length === 0 ? (
            <Empty description="暂无评论，来发表第一条评论吧！" />
          ) : (
            comments.map((comment) => (
              <div
                key={comment.id}
                style={{
                  marginBottom: 16,
                  paddingBottom: 16,
                  borderBottom: '1px solid #f0f0f0',
                }}
              >
                <div style={{ display: 'flex', alignItems: 'flex-start' }}>
                  <Avatar
                    src={comment.user?.avatar}
                    style={{ backgroundColor: '#1890ff', marginRight: 12 }}
                  >
                    {comment.user?.username?.[0]?.toUpperCase()}
                  </Avatar>
                  <div style={{ flex: 1 }}>
                    <div style={{ marginBottom: 4 }}>
                      <strong>{comment.user?.username}</strong>
                      <span style={{ marginLeft: 12, fontSize: 12, color: '#999' }}>
                        {new Date(comment.createTime).toLocaleString()}
                      </span>
                    </div>
                    <p style={{ marginBottom: 8, color: '#333' }}>{comment.content}</p>
                    <Space size={12}>
                      <Button
                        type="text"
                        size="small"
                        icon={comment.isLiked ? <LikeFilled style={{ color: '#1890ff' }} /> : <LikeOutlined />}
                        onClick={() => handleCommentLike(comment.id, comment.isLiked || false)}
                      >
                        {comment.likeCount || 0}
                      </Button>
                    </Space>
                  </div>
                </div>
              </div>
            ))
          )}
        </Spin>
      </Card>
    </div>
  );
};

export default ContentDetailPage;

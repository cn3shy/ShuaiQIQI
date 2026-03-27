import React from 'react';
import { Card, Avatar, Space, Tag, Button, Row, Col } from 'antd';
import { LikeOutlined, CommentOutlined, StarOutlined } from '@ant-design/icons';
import type { Content } from '@types';
import { Link } from 'react-router-dom';

interface ContentCardProps {
  content: Content;
  onLike?: (id: string) => void;
  onFavorite?: (id: string) => void;
}

/**
 * 卡片组件 - 用于内容列表展示
 */
const ContentCard: React.FC<ContentCardProps> = ({ content, onLike, onFavorite }) => {
  return (
    <Card
      hoverable
      cover={
        content.coverImage ? (
          <div style={{ height: 200, overflow: 'hidden' }}>
            <img
              alt={content.title}
              src={content.coverImage}
              style={{ width: '100%', height: '100%', objectFit: 'cover' }}
            />
          </div>
        ) : null
      }
      style={{ marginBottom: 16, borderRadius: 8 }}
      bodyStyle={{ padding: 16 }}
    >
      <div style={{ marginBottom: 12 }}>
        <Link to={`/content/${content.id}`}>
          <h3 style={{ margin: 0, fontSize: 16, fontWeight: 500, color: '#333' }}>
            {content.title}
          </h3>
        </Link>
        <p style={{ margin: '8px 0', color: '#666', fontSize: 14, lineHeight: 1.5 }}>
          {content.summary}
        </p>
      </div>

      <Space size={8} style={{ marginBottom: 12, flexWrap: 'wrap' }}>
        <Avatar src={content.author.avatar} size={24} style={{ backgroundColor: '#1890ff' }}>
          {content.author.username[0]?.toUpperCase()}
        </Avatar>
        <span style={{ fontSize: 12, color: '#666' }}>{content.author.username}</span>
        <span style={{ fontSize: 12, color: '#999' }}>
          {new Date(content.createTime).toLocaleDateString()}
        </span>
      </Space>

      {content.tags && content.tags.length > 0 && (
        <div style={{ marginBottom: 12 }}>
          {content.tags.map((tag) => (
            <Tag key={tag} color="blue" style={{ fontSize: 12 }}>
              {tag}
            </Tag>
          ))}
        </div>
      )}

      <Row justify="space-between" align="middle">
        <Col>
          <Space size={16}>
            <Button
              type="text"
              icon={<LikeOutlined style={{ color: content.isLiked ? '#1890ff' : '#666' }} />}
              size="small"
              onClick={() => onLike?.(content.id)}
            >
              {content.likeCount}
            </Button>
            <Button
              type="text"
              icon={<CommentOutlined style={{ color: '#666' }} />}
              size="small"
              onClick={() => console.log('评论:', content.id)}
            >
              {content.commentCount}
            </Button>
            <Button
              type="text"
              icon={<StarOutlined style={{ color: content.isFavorited ? '#faad14' : '#666' }} />}
              size="small"
              onClick={() => onFavorite?.(content.id)}
            >
              {content.favoriteCount}
            </Button>
          </Space>
        </Col>
        <Col>
          <Link to={`/content/${content.id}`}>
            <Button type="link">
              查看更多
            </Button>
          </Link>
        </Col>
      </Row>
    </Card>
  );
};

export default ContentCard;
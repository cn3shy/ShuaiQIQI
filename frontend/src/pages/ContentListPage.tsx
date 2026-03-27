import React, { useState, useEffect } from 'react';
import { Row, Col, Spin, Empty, Select, Input, Card, Space } from 'antd';
import { SearchOutlined } from '@ant-design/icons';
import ContentCard from '@components/ContentCard';
import { getCategoryList, getContentList, likeContent, unlikeContent, favoriteContent, unfavoriteContent } from '@services/content';
import type { Content, Category } from '@types';

/**
 * 内容列表页
 */
const ContentListPage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [contents, setContents] = useState<Content[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [selectedCategory, setSelectedCategory] = useState<string>();
  const [keyword, setKeyword] = useState('');
  const [sortBy, setSortBy] = useState<'latest' | 'popular' | 'hot'>('latest');

  const loadCategories = async () => {
    try {
      const data = await getCategoryList();
      setCategories(data.data || []);
    } catch (error) {
      console.error('加载分类失败:', error);
    }
  };

  const loadContents = async () => {
    setLoading(true);
    try {
      const params: any = { sortBy };
      if (selectedCategory) params.categoryId = selectedCategory;
      if (keyword) params.keyword = keyword;

      const data = await getContentList(params);
      setContents(data.data?.list || []);
    } catch (error) {
      console.error('加载内容失败:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleLike = async (id: string) => {
    try {
      const content = contents.find(c => c.id === id);
      if (!content) return;

      if (content.isLiked) {
        await unlikeContent(id);
      } else {
        await likeContent(id);
      }

      setContents(prev => prev.map(c => {
        if (c.id === id) {
          return {
            ...c,
            isLiked: !c.isLiked,
            likeCount: c.isLiked ? c.likeCount - 1 : c.likeCount + 1,
          };
        }
        return c;
      }));
    } catch (error) {
      console.error('点赞操作失败:', error);
    }
  };

  const handleFavorite = async (id: string) => {
    try {
      const content = contents.find(c => c.id === id);
      if (!content) return;

      if (content.isFavorited) {
        await unfavoriteContent(id);
      } else {
        await favoriteContent(id);
      }

      setContents(prev => prev.map(c => {
        if (c.id === id) {
          return {
            ...c,
            isFavorited: !c.isFavorited,
            favoriteCount: c.isFavorited ? c.favoriteCount - 1 : c.favoriteCount + 1,
          };
        }
        return c;
      }));
    } catch (error) {
      console.error('收藏操作失败:', error);
    }
  };

  useEffect(() => {
    loadCategories();
  }, []);

  useEffect(() => {
    loadContents();
  }, [selectedCategory, sortBy]);

  return (
    <div style={{ padding: 24 }}>
      <Card style={{ marginBottom: 16 }}>
        <Input.Search
          placeholder="搜索内容..."
          prefix={<SearchOutlined />}
          onSearch={(value) => {
            setKeyword(value);
            loadContents();
          }}
          allowClear
          size="large"
          style={{ maxWidth: 400 }}
        />
      </Card>

      <Card style={{ marginBottom: 16 }}>
        <Space size={16}>
          <Select
            placeholder="全部分类"
            allowClear
            value={selectedCategory}
            onChange={(value) => setSelectedCategory(value)}
            style={{ width: 200 }}
          >
            {categories.map((category) => (
              <Select.Option key={category.id} value={category.id}>
                {category.name}
              </Select.Option>
            ))}
          </Select>

          <Select
            placeholder="排序方式"
            value={sortBy}
            onChange={setSortBy}
            style={{ width: 200 }}
          >
            <Select.Option value="latest">最新发布</Select.Option>
            <Select.Option value="popular">最多点赞</Select.Option>
            <Select.Option value="hot">最热</Select.Option>
          </Select>
        </Space>
      </Card>

      <Spin spinning={loading}>
        {contents.length === 0 ? (
          <Empty description="暂无内容" />
        ) : (
          <Row gutter={[16, 16]}>
            {contents.map((content) => (
              <Col xs={24} sm={24} md={12} lg={12} xl={8} key={content.id}>
                <ContentCard
                  content={content}
                  onLike={handleLike}
                  onFavorite={handleFavorite}
                />
              </Col>
            ))}
          </Row>
        )}
      </Spin>
    </div>
  );
};

export default ContentListPage;
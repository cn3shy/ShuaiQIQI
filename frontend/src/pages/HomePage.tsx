/**
 * 主页
 */
import React, { useState, useEffect, useRef } from 'react';
import { Row, Col, Empty, Spin, Tabs, Input, Card } from 'antd';
import { SearchOutlined } from '@ant-design/icons';
import ContentCard from '@components/ContentCard';
import { getContentList, getHotContent, getRecommendContent, likeContent, unlikeContent, favoriteContent, unfavoriteContent } from '@services/content';
import { Link } from 'react-router-dom';
import type { Content } from '@types';
import { useSearchHistory } from '@hooks/useSearchHistory';
import SearchHistory from '@components/SearchHistory';

const HomePage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [contents, setContents] = useState<Content[]>([]);
  const [activeTab, setActiveTab] = useState('latest');
  const [keyword, setKeyword] = useState('');
  const [showHistory, setShowHistory] = useState(false);
  const { history, addHistory, removeHistory, clearHistory } = useSearchHistory();
  const abortControllerRef = useRef<AbortController | null>(null);

  const loadContents = async () => {
    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
    }
    const controller = new AbortController();
    abortControllerRef.current = controller;

    setLoading(true);
    try {
      let data;
      const params: any = { page: 1, pageSize: 20 };
      if (keyword) params.keyword = keyword;

      switch (activeTab) {
        case 'hot':
          data = await getHotContent(params);
          break;
        case 'recommend':
          data = await getRecommendContent(params);
          break;
        default:
          data = await getContentList({ ...params, sortBy: 'latest' });
      }
      if (!controller.signal.aborted) {
        setContents(data.data?.list || []);
      }
    } catch (error) {
      if (!controller.signal.aborted) {
        console.error('加载内容失败:', error);
      }
    } finally {
      if (!controller.signal.aborted) {
        setLoading(false);
      }
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
    loadContents();
    return () => {
      if (abortControllerRef.current) {
        abortControllerRef.current.abort();
      }
    };
  }, [activeTab]);

  const tabItems = [
    { key: 'latest', label: '最新' },
    { key: 'hot', label: '热门' },
    { key: 'recommend', label: '推荐' },
  ];

  return (
    <div style={{ padding: 24 }}>
      <div style={{ marginBottom: 16 }}>
        <Input
          placeholder="搜索内容..."
          prefix={<SearchOutlined />}
          size="large"
          allowClear
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          onFocus={() => setShowHistory(true)}
          onBlur={() => setTimeout(() => setShowHistory(false), 200)}
          onPressEnter={() => {
            if (keyword.trim()) {
              addHistory(keyword);
              loadContents();
              setShowHistory(false);
            }
          }}
        />
        {showHistory && (
          <SearchHistory
            history={history}
            onSelect={(kw) => {
              setKeyword(kw);
              addHistory(kw);
              loadContents();
              setShowHistory(false);
            }}
            onRemove={removeHistory}
            onClear={clearHistory}
          />
        )}
      </div>

      <Tabs
        activeKey={activeTab}
        onChange={setActiveTab}
        items={tabItems}
        style={{ marginBottom: 16 }}
      />

      <Spin spinning={loading}>
        {contents.length === 0 ? (
          <Empty description="暂无内容" />
        ) : (
          <Row gutter={[16, 16]}>
            <Col xs={24} sm={24} md={16} lg={16} xl={16}>
              {contents.map((content) => (
                <ContentCard
                  key={content.id}
                  content={content}
                  onLike={handleLike}
                  onFavorite={handleFavorite}
                />
              ))}
            </Col>
            <Col xs={0} sm={0} md={8} lg={8} xl={8}>
              <Card title="热门标签" style={{ marginBottom: 16 }}>
                <div>
                  <Link to="/content?category=1">技术</Link>
                  <Link to="/content?category=2">生活</Link>
                  <Link to="/content?category=3">娱乐</Link>
                </div>
              </Card>
              <Card title="推荐作者">
                <div>暂无数据</div>
              </Card>
            </Col>
          </Row>
        )}
      </Spin>
    </div>
  );
};

export default HomePage;
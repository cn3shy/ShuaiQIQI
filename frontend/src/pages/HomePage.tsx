/**
 * 主页
 */
import React, { useState, useEffect } from 'react';
import { Row, Col, Empty, Spin, Tabs, Input, Card } from 'antd';
import { SearchOutlined } from '@ant-design/icons';
import ContentCard from '@components/ContentCard';
import { getContentList, getHotContent, getRecommendContent } from '@services/content';
import { Link } from 'react-router-dom';
import type { Content } from '@types';

const HomePage: React.FC = () => {
  const [loading, setLoading] = useState(false);
  const [contents, setContents] = useState<Content[]>([]);
  const [activeTab, setActiveTab] = useState('latest');

  const loadContents = async () => {
    setLoading(true);
    try {
      let data;
      switch (activeTab) {
        case 'hot':
          data = await getHotContent({ page: 1, pageSize: 20 });
          break;
        case 'recommend':
          data = await getRecommendContent({ page: 1, pageSize: 20 });
          break;
        default:
          data = await getContentList({ page: 1, pageSize: 20, sortBy: 'latest' });
      }
      setContents(data.data?.list || []);
    } catch (error) {
      console.error('加载内容失败:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadContents();
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
          onPressEnter={() => {}}
        />
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
                  onLike={() => console.log('点赞:', content.id)}
                  onFavorite={() => console.log('收藏:', content.id)}
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
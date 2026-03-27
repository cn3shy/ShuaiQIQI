// 搜索历史组件
import React from 'react';
import { Tag, Space, Button } from 'antd';
import { DeleteOutlined, ClockCircleOutlined } from '@ant-design/icons';

interface SearchHistoryProps {
  history: string[];
  onSelect: (keyword: string) => void;
  onClear: () => void;
  onRemove: (keyword: string) => void;
}

const SearchHistory: React.FC<SearchHistoryProps> = ({
  history,
  onSelect,
  onClear,
  onRemove,
}) => {
  if (history.length === 0) {
    return null;
  }

  return (
    <div style={{ marginTop: 8 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
        <span style={{ color: '#999', fontSize: 12 }}>
          <ClockCircleOutlined style={{ marginRight: 4 }} />
          搜索历史
        </span>
        <Button type="link" size="small" onClick={onClear} icon={<DeleteOutlined />}>
          清空
        </Button>
      </div>
      <Space size={[8, 8]} wrap>
        {history.map((item) => (
          <Tag
            key={item}
            closable
            onClose={(e) => {
              e.preventDefault();
              onRemove(item);
            }}
            style={{ cursor: 'pointer' }}
            onClick={() => onSelect(item)}
          >
            {item}
          </Tag>
        ))}
      </Space>
    </div>
  );
};

export default SearchHistory;

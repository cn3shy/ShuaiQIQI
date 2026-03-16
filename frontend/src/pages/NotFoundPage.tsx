import React, { useState, useEffect } from 'react';
import { Result, Button, Space } from 'antd';
import { Link } from 'react-router-dom';

/**
 * 404页面
 */
const NotFoundPage: React.FC = () => {
  return (
    <div style={{ minHeight: '100vh', display: 'flex', justifyContent: 'center', alignItems: 'center', background: '#f0f2f5' }}>
      <Result
        status="404"
        title="404"
        subTitle="抱歉，您访问的页面不存在"
        extra={
          <Link to="/">
            <Button type="primary">返回首页</Button>
          </Link>
        }
      />
    </div>
  );
};

export default NotFoundPage;
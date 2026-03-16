import React, { useState } from 'react';
import { Card, Upload, Button, Form, Input, message } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import { createContent } from '@services/content';
import { useNavigate } from 'react-router-dom';

/**
 * 创建内容页
 */
const CreateContentPage: React.FC = () => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleFinish = async (values: any) => {
    setLoading(true);
    try {
      await createContent(values);
      message.success('内容发布成功');
      navigate('/');
    } catch (error: any) {
      message.error(error.message || '发布失败');
    } finally {
      setLoading(false);
    }
  };

  const beforeUpload = (file: File) => {
    const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png';
    if (!isJpgOrPng) {
      message.error('只能上传 JPG/PNG 文件!');
    }
    const isLt2M = file.size / 1024 / 1024 < 2;
    if (!isLt2M) {
      message.error('图片大小不能超过2MB!');
    }
    return isJpgOrPng && isLt2M;
  };

  return (
    <div style={{ maxWidth: 800, margin: '0 auto', padding: 24 }}>
      <h2 style={{ marginBottom: 24 }}>发布内容</h2>

      <Card>
        <Form form={form} layout="vertical" onFinish={handleFinish}>
          <Form.Item
            label="标题"
            name="title"
            rules={[
              { required: true, message: '请输入标题' },
              { max: 50, message: '标题不能超过50个字' },
            ]}
          >
            <Input placeholder="请输入标题" showCount maxLength={50} size="large" />
          </Form.Item>

          <Form.Item label="封面图片" name="coverImage">
            <Upload
              name="file"
              listType="picture-card"
              className="avatar-uploader"
              showUploadList={false}
              beforeUpload={beforeUpload}
              customRequest={({ file, onSuccess }) => {
                console.log('上传图片:', file);
                setTimeout(() => onSuccess?.('ok', new XMLHttpRequest()), 1000);
              }}
            >
              <div>
                <PlusOutlined />
                <div style={{ marginTop: 8 }}>上传封面</div>
              </div>
            </Upload>
          </Form.Item>

          <Form.Item label="分类" name="categoryId">
            <Input placeholder="选择分类" />
          </Form.Item>

          <Form.Item label="标签" name="tags">
            <Input placeholder="输入标签，用逗号分隔" />
          </Form.Item>

          <Form.Item
            label="简介"
            name="summary"
            rules={[
              { required: true, message: '请输入简介' },
              { max: 200, message: '简介不能超过200个字' },
            ]}
          >
            <Input.TextArea
              placeholder="请输入内容简介"
              rows={3}
              showCount
              maxLength={200}
            />
          </Form.Item>

          <Form.Item
            label="内容"
            name="content"
            rules={[{ required: true, message: '请输入内容' }]}
          >
            <Input.TextArea
              placeholder="请输入内容"
              rows={12}
            />
          </Form.Item>

          <Form.Item>
            <Button type="primary" htmlType="submit" loading={loading} size="large" block>
              立即发布
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </div>
  );
};

export default CreateContentPage;
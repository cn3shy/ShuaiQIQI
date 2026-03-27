import React, { useState, useEffect } from 'react';
import { Card, Upload, Button, Form, Input, Select, message, Spin } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { createContent, getCategoryList } from '@services/content';
import type { Category } from '@types';

/**
 * 创建内容页
 */
const CreateContentPage: React.FC = () => {
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [categories, setCategories] = useState<Category[]>([]);
  const [categoriesLoading, setCategoriesLoading] = useState(false);
  const [coverImage, setCoverImage] = useState<string>('');
  const navigate = useNavigate();

  useEffect(() => {
    loadCategories();
  }, []);

  const loadCategories = async () => {
    setCategoriesLoading(true);
    try {
      const response = await getCategoryList();
      setCategories(response.data || []);
    } catch (error) {
      console.error('加载分类失败:', error);
    } finally {
      setCategoriesLoading(false);
    }
  };

  const handleFinish = async (values: { title: string; categoryId?: string; tags?: string; summary: string; content: string }) => {
    setLoading(true);
    try {
      const data = {
        ...values,
        coverImage: coverImage || undefined,
        tags: values.tags ? values.tags.split(',').map((t) => t.trim()).filter(Boolean) : undefined,
      };
      const response = await createContent(data);
      message.success('内容发布成功');
      navigate(`/content/${response.data.id}`);
    } catch (error) {
      const err = error as Error;
      message.error(err.message || '发布失败');
    } finally {
      setLoading(false);
    }
  };

  const beforeUpload = (file: File) => {
    const isImage = file.type.startsWith('image/');
    if (!isImage) {
      message.error('只能上传图片文件!');
      return false;
    }
    const isLt2M = file.size / 1024 / 1024 < 2;
    if (!isLt2M) {
      message.error('图片大小不能超过2MB!');
      return false;
    }
    return true;
  };

  const handleUpload = async (file: File) => {
    // 模拟上传 - 实际项目中应该调用上传API
    const reader = new FileReader();
    reader.onload = (e) => {
      const url = e.target?.result as string;
      setCoverImage(url);
      message.success('封面上传成功');
    };
    reader.readAsDataURL(file);
    return false; // 阻止默认上传
  };

  return (
    <div style={{ maxWidth: 800, margin: '0 auto', padding: 24 }}>
      <h2 style={{ marginBottom: 24 }}>发布内容</h2>

      <Card>
        <Spin spinning={categoriesLoading}>
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

            <Form.Item label="封面图片">
              <Upload
                name="file"
                listType="picture-card"
                showUploadList={false}
                beforeUpload={beforeUpload}
                customRequest={({ file }) => handleUpload(file as File)}
              >
                {coverImage ? (
                  <img src={coverImage} alt="封面" style={{ width: '100%' }} />
                ) : (
                  <div>
                    <PlusOutlined />
                    <div style={{ marginTop: 8 }}>上传封面</div>
                  </div>
                )}
              </Upload>
              {coverImage && (
                <Button type="link" onClick={() => setCoverImage('')} style={{ marginTop: 8 }}>
                  移除封面
                </Button>
              )}
            </Form.Item>

            <Form.Item label="分类" name="categoryId">
              <Select
                placeholder="选择分类"
                allowClear
                options={categories.map((cat) => ({
                  label: cat.name,
                  value: cat.id,
                }))}
              />
            </Form.Item>

            <Form.Item label="标签" name="tags" extra="多个标签用逗号分隔">
              <Input placeholder="例如：技术,前端,React" />
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
                showCount
              />
            </Form.Item>

            <Form.Item>
              <Button type="primary" htmlType="submit" loading={loading} size="large" block>
                立即发布
              </Button>
            </Form.Item>
          </Form>
        </Spin>
      </Card>
    </div>
  );
};

export default CreateContentPage;

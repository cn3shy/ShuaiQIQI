import { useState, useCallback } from 'react';
import { likeContent, unlikeContent, favoriteContent, unfavoriteContent } from '@services/content';

interface UseContentInteractionOptions<T extends { id: string; isLiked: boolean; isFavorited: boolean; likeCount: number; favoriteCount: number }> {
  items: T[];
  setItems: React.Dispatch<React.SetStateAction<T[]>>;
}

export function useContentInteraction<T extends { id: string; isLiked: boolean; isFavorited: boolean; likeCount: number; favoriteCount: number }>({
  items,
  setItems,
}: UseContentInteractionOptions<T>) {
  const [interactingId, setInteractingId] = useState<string | null>(null);

  const handleLike = useCallback(async (id: string) => {
    const item = items.find(c => c.id === id);
    if (!item || interactingId) return;

    setInteractingId(id);
    try {
      if (item.isLiked) {
        await unlikeContent(id);
      } else {
        await likeContent(id);
      }
      setItems(prev => prev.map(c =>
        c.id === id
          ? { ...c, isLiked: !c.isLiked, likeCount: c.isLiked ? c.likeCount - 1 : c.likeCount + 1 }
          : c
      ));
    } catch {
      console.error('点赞操作失败');
    } finally {
      setInteractingId(null);
    }
  }, [items, setItems, interactingId]);

  const handleFavorite = useCallback(async (id: string) => {
    const item = items.find(c => c.id === id);
    if (!item || interactingId) return;

    setInteractingId(id);
    try {
      if (item.isFavorited) {
        await unfavoriteContent(id);
      } else {
        await favoriteContent(id);
      }
      setItems(prev => prev.map(c =>
        c.id === id
          ? { ...c, isFavorited: !c.isFavorited, favoriteCount: c.isFavorited ? c.favoriteCount - 1 : c.favoriteCount + 1 }
          : c
      ));
    } catch {
      console.error('收藏操作失败');
    } finally {
      setInteractingId(null);
    }
  }, [items, setItems, interactingId]);

  return { handleLike, handleFavorite };
}

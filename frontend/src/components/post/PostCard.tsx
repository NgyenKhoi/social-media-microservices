import { useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { Heart, MessageCircle, Share2, Bookmark, MoreHorizontal } from "lucide-react";
import EmojiReactionPicker from "./EmojiReactionPicker";

interface PostCardProps {
  id: number;
  avatar: string;
  username: string;
  handle: string;
  content: string;
  image?: string;
  likes: number;
  comments: number;
  shares: number;
  timestamp: string;
}

const PostCard = ({
  avatar,
  username,
  handle,
  content,
  image,
  likes,
  comments,
  shares,
  timestamp,
}: PostCardProps) => {
  const [isLiked, setIsLiked] = useState(false);
  const [isBookmarked, setIsBookmarked] = useState(false);
  const [selectedEmoji, setSelectedEmoji] = useState<string | null>(null);
  const [likeCount, setLikeCount] = useState(likes);

  const handleLike = () => {
    setIsLiked(!isLiked);
    setLikeCount((prev) => (isLiked ? prev - 1 : prev + 1));
  };

  return (
    <motion.article
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      whileHover={{ y: -2 }}
      transition={{ type: "spring", stiffness: 300, damping: 30 }}
      className="glass rounded-2xl p-5 mb-4"
    >
      {/* Header */}
      <div className="flex items-start justify-between mb-4">
        <div className="flex items-center gap-3">
          <motion.div
            whileHover={{ scale: 1.05 }}
            className="relative"
          >
            <img
              src={avatar}
              alt={username}
              className="w-12 h-12 rounded-full object-cover ring-2 ring-accent/50"
            />
            <div className="absolute -bottom-1 -right-1 w-4 h-4 bg-green-500 rounded-full border-2 border-card" />
          </motion.div>
          <div>
            <h3 className="font-semibold text-foreground">{username}</h3>
            <p className="text-sm text-muted-foreground">
              @{handle} · {timestamp}
            </p>
          </div>
        </div>
        <motion.button
          whileHover={{ scale: 1.1 }}
          whileTap={{ scale: 0.9 }}
          className="p-2 rounded-full hover:bg-muted/50 transition-colors"
        >
          <MoreHorizontal className="w-5 h-5 text-muted-foreground" />
        </motion.button>
      </div>

      {/* Content */}
      <p className="text-foreground mb-4 leading-relaxed">{content}</p>

      {/* Image */}
      {image && (
        <motion.div
          whileHover={{ scale: 1.01 }}
          className="mb-4 rounded-xl overflow-hidden"
        >
          <img
            src={image}
            alt="Post content"
            className="w-full h-64 object-cover"
          />
        </motion.div>
      )}

      {/* Emoji Reaction */}
      <div className="mb-4">
        <EmojiReactionPicker
          onSelect={setSelectedEmoji}
          selectedEmoji={selectedEmoji}
        />
      </div>

      {/* Actions */}
      <div className="flex items-center justify-between pt-4 border-t border-border/50">
        <div className="flex items-center gap-1">
          <motion.button
            whileHover={{ scale: 1.1 }}
            whileTap={{ scale: 0.9 }}
            onClick={handleLike}
            className="flex items-center gap-2 px-3 py-2 rounded-xl hover:bg-muted/50 transition-colors"
          >
            <AnimatePresence mode="wait">
              <motion.div
                key={isLiked ? "liked" : "not-liked"}
                initial={{ scale: 0 }}
                animate={{ scale: 1 }}
                exit={{ scale: 0 }}
                transition={{ type: "spring", stiffness: 500 }}
              >
                <Heart
                  className={`w-5 h-5 ${
                    isLiked
                      ? "fill-red-500 text-red-500"
                      : "text-muted-foreground"
                  }`}
                />
              </motion.div>
            </AnimatePresence>
            <span className={`text-sm font-medium ${isLiked ? "text-red-500" : "text-muted-foreground"}`}>
              {likeCount}
            </span>
          </motion.button>

          <motion.button
            whileHover={{ scale: 1.1 }}
            whileTap={{ scale: 0.9 }}
            className="flex items-center gap-2 px-3 py-2 rounded-xl hover:bg-muted/50 transition-colors"
          >
            <MessageCircle className="w-5 h-5 text-muted-foreground" />
            <span className="text-sm font-medium text-muted-foreground">{comments}</span>
          </motion.button>

          <motion.button
            whileHover={{ scale: 1.1 }}
            whileTap={{ scale: 0.9 }}
            className="flex items-center gap-2 px-3 py-2 rounded-xl hover:bg-muted/50 transition-colors"
          >
            <Share2 className="w-5 h-5 text-muted-foreground" />
            <span className="text-sm font-medium text-muted-foreground">{shares}</span>
          </motion.button>
        </div>

        <motion.button
          whileHover={{ scale: 1.1 }}
          whileTap={{ scale: 0.9 }}
          onClick={() => setIsBookmarked(!isBookmarked)}
          className="p-2 rounded-xl hover:bg-muted/50 transition-colors"
        >
          <Bookmark
            className={`w-5 h-5 ${
              isBookmarked
                ? "fill-primary text-primary"
                : "text-muted-foreground"
            }`}
          />
        </motion.button>
      </div>
    </motion.article>
  );
};

export default PostCard;

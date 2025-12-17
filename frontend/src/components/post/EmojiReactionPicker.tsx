import { useState } from "react";
import { motion, AnimatePresence, Variants, TargetAndTransition } from "framer-motion";

const emojis = ["❤️", "😂", "😮", "😢", "😡", "👍"];

interface EmojiReactionPickerProps {
  onSelect: (emoji: string) => void;
  selectedEmoji: string | null;
}

const EmojiReactionPicker = ({ onSelect, selectedEmoji }: EmojiReactionPickerProps) => {
  const [isOpen, setIsOpen] = useState(false);

  const containerVariants: Variants = {
    hidden: { opacity: 0, scale: 0.8, y: 10 },
    visible: {
      opacity: 1,
      scale: 1,
      y: 0,
      transition: {
        type: "spring" as const,
        stiffness: 400,
        damping: 25,
        staggerChildren: 0.05,
      },
    },
    exit: {
      opacity: 0,
      scale: 0.8,
      y: 10,
      transition: { duration: 0.15 },
    },
  };

  const emojiVariants: Variants = {
    hidden: { opacity: 0, y: 20, scale: 0 },
    visible: {
      opacity: 1,
      y: 0,
      scale: 1,
      transition: { type: "spring" as const, stiffness: 500, damping: 25 },
    },
  };

  const floatAnimation: TargetAndTransition = {
    y: [0, -8, 0],
    transition: {
      duration: 1.5,
      repeat: Infinity,
      ease: "easeInOut" as const,
    },
  };

  return (
    <div
      className="relative"
      onMouseEnter={() => setIsOpen(true)}
      onMouseLeave={() => setIsOpen(false)}
    >
      <motion.button
        whileHover={{ scale: 1.1 }}
        whileTap={{ scale: 0.9 }}
        className="flex items-center gap-2 px-4 py-2 rounded-full bg-muted/50 hover:bg-muted transition-colors"
      >
        <motion.span
          animate={selectedEmoji ? floatAnimation : undefined}
          className="text-xl"
        >
          {selectedEmoji || "😊"}
        </motion.span>
        <span className="text-sm font-medium text-muted-foreground">
          {selectedEmoji ? "You reacted" : "React"}
        </span>
      </motion.button>

      <AnimatePresence>
        {isOpen && (
          <motion.div
            variants={containerVariants}
            initial="hidden"
            animate="visible"
            exit="exit"
            className="absolute bottom-full left-0 mb-2 p-2 rounded-2xl glass shadow-xl"
          >
            <div className="flex gap-1">
              {emojis.map((emoji) => (
                <motion.button
                  key={emoji}
                  variants={emojiVariants}
                  whileHover={{
                    scale: 1.4,
                    y: -8,
                    transition: { type: "spring" as const, stiffness: 400 },
                  }}
                  whileTap={{ scale: 0.8 }}
                  onClick={() => {
                    onSelect(selectedEmoji === emoji ? "" : emoji);
                    setIsOpen(false);
                  }}
                  className={`w-10 h-10 flex items-center justify-center rounded-xl text-2xl transition-colors ${
                    selectedEmoji === emoji
                      ? "bg-primary/20"
                      : "hover:bg-muted"
                  }`}
                >
                  {emoji}
                </motion.button>
              ))}
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
};

export default EmojiReactionPicker;

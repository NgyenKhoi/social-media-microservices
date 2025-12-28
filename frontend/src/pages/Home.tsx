import { motion } from "framer-motion";
import Navbar from "@/components/layout/Navbar";
import PostCard from "@/components/post/PostCard";

const mockPosts = [
  {
    id: 1,
    avatar: "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=150&h=150&fit=crop",
    username: "Sarah Chen",
    handle: "sarahc",
    content: "Just finished my morning meditation session 🧘‍♀️ Starting the day with positive vibes! What's everyone's morning routine?",
    likes: 234,
    comments: 45,
    shares: 12,
    timestamp: "2h",
  },
  {
    id: 2,
    avatar: "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150&h=150&fit=crop",
    username: "Alex Rivera",
    handle: "alexr",
    content: "The sunset today was absolutely breathtaking! Nature never fails to amaze me 🌅",
    image: "https://images.unsplash.com/photo-1495616811223-4d98c6e9c869?w=600&h=400&fit=crop",
    likes: 892,
    comments: 123,
    shares: 67,
    timestamp: "4h",
  },
  {
    id: 3,
    avatar: "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=150&h=150&fit=crop",
    username: "Maya Johnson",
    handle: "mayaj",
    content: "Finally launched my new project after months of hard work! 🚀 Can't wait to share more details with you all. Stay tuned!",
    likes: 1542,
    comments: 234,
    shares: 89,
    timestamp: "6h",
  },
  {
    id: 4,
    avatar: "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=150&h=150&fit=crop",
    username: "David Kim",
    handle: "davidk",
    content: "Coffee and coding - the perfect combo ☕💻 Working on something exciting today!",
    image: "https://images.unsplash.com/photo-1497935586351-b67a49e012bf?w=600&h=400&fit=crop",
    likes: 456,
    comments: 78,
    shares: 23,
    timestamp: "8h",
  },
  {
    id: 5,
    avatar: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150&h=150&fit=crop",
    username: "Emma Wilson",
    handle: "emmaw",
    content: "Remember: Your vibe attracts your tribe ✨ Surround yourself with people who lift you up and inspire you to be the best version of yourself.",
    likes: 2341,
    comments: 456,
    shares: 234,
    timestamp: "12h",
  },
];

const Home = () => {
  return (
    <div className="min-h-screen bg-background">
      <Navbar />
      
      <main className="container mx-auto px-4 pt-24 pb-12">
        <div className="max-w-xl mx-auto">
          {/* Welcome Header */}
          <motion.div
            initial={{ opacity: 0, y: -20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.2 }}
            className="text-center mb-8"
          >
            <h1 className="text-3xl font-bold text-gradient mb-2">Your Feed</h1>
            <p className="text-muted-foreground">See what's vibing today</p>
          </motion.div>

          {/* Post Feed */}
          <motion.div
            initial="hidden"
            animate="visible"
            variants={{
              hidden: { opacity: 0 },
              visible: {
                opacity: 1,
                transition: { staggerChildren: 0.1 },
              },
            }}
          >
            {mockPosts.map((post, index) => (
              <motion.div
                key={post.id}
                variants={{
                  hidden: { opacity: 0, y: 20 },
                  visible: { opacity: 1, y: 0 },
                }}
                transition={{ delay: index * 0.1 }}
              >
                <PostCard {...post} />
              </motion.div>
            ))}
          </motion.div>
        </div>
      </main>
    </div>
  );
};

export default Home;

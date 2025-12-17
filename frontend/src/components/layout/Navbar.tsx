import { useState } from "react";
import { motion } from "framer-motion";
import { Sparkles } from "lucide-react";
import { Button } from "@/components/ui/button";
import LoginModal from "@/components/auth/LoginModal";

const Navbar = () => {
  const [isLoginOpen, setIsLoginOpen] = useState(false);

  return (
    <>
      <motion.nav
        initial={{ y: -100 }}
        animate={{ y: 0 }}
        transition={{ type: "spring", stiffness: 100, damping: 20 }}
        className="fixed top-0 left-0 right-0 z-50 glass"
      >
        <div className="container mx-auto px-4 py-3 flex items-center justify-between">
          <motion.div
            className="flex items-center gap-2"
            whileHover={{ scale: 1.02 }}
            whileTap={{ scale: 0.98 }}
          >
            <div className="w-10 h-10 rounded-xl gradient-primary flex items-center justify-center">
              <Sparkles className="w-5 h-5 text-primary-foreground" />
            </div>
            <span className="text-xl font-bold text-gradient">Vibes</span>
          </motion.div>

          <motion.div
            whileHover={{ scale: 1.05 }}
            whileTap={{ scale: 0.95 }}
          >
            <Button
              onClick={() => setIsLoginOpen(true)}
              className="gradient-primary text-primary-foreground font-semibold px-6 rounded-xl shadow-lg hover:shadow-xl transition-shadow"
            >
              Login
            </Button>
          </motion.div>
        </div>
      </motion.nav>

      <LoginModal open={isLoginOpen} onOpenChange={setIsLoginOpen} />
    </>
  );
};

export default Navbar;

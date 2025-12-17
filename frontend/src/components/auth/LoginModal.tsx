import { motion, AnimatePresence } from "framer-motion";
import { X, Mail, Lock, Sparkles } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";

interface LoginModalProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

const LoginModal = ({ open, onOpenChange }: LoginModalProps) => {
  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    // Demo only - no actual auth logic
    onOpenChange(false);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md glass border-0 overflow-hidden">
        <motion.div
          initial={{ opacity: 0, scale: 0.95 }}
          animate={{ opacity: 1, scale: 1 }}
          exit={{ opacity: 0, scale: 0.95 }}
          transition={{ type: "spring", stiffness: 300, damping: 30 }}
        >
          <DialogHeader className="text-center pb-4">
            <div className="mx-auto w-16 h-16 rounded-2xl gradient-primary flex items-center justify-center mb-4">
              <Sparkles className="w-8 h-8 text-primary-foreground" />
            </div>
            <DialogTitle className="text-2xl font-bold text-gradient">
              Welcome Back
            </DialogTitle>
            <p className="text-muted-foreground text-sm mt-2">
              Sign in to continue sharing vibes
            </p>
          </DialogHeader>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="email" className="text-foreground font-medium">
                Email
              </Label>
              <div className="relative">
                <Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
                <Input
                  id="email"
                  type="email"
                  placeholder="you@example.com"
                  className="pl-10 bg-muted/50 border-border focus:border-primary rounded-xl"
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="password" className="text-foreground font-medium">
                Password
              </Label>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
                <Input
                  id="password"
                  type="password"
                  placeholder="••••••••"
                  className="pl-10 bg-muted/50 border-border focus:border-primary rounded-xl"
                />
              </div>
            </div>

            <motion.div
              whileHover={{ scale: 1.02 }}
              whileTap={{ scale: 0.98 }}
            >
              <Button
                type="submit"
                className="w-full gradient-primary text-primary-foreground font-semibold py-6 rounded-xl shadow-lg hover:shadow-xl transition-all"
              >
                Sign In
              </Button>
            </motion.div>

            <p className="text-center text-sm text-muted-foreground">
              Don't have an account?{" "}
              <button type="button" className="text-primary font-medium hover:underline">
                Sign up
              </button>
            </p>
          </form>
        </motion.div>
      </DialogContent>
    </Dialog>
  );
};

export default LoginModal;

use jni::objects::{GlobalRef, JMethodID};
use jni::signature::{Primitive, ReturnType};
use jni::{JNIEnv, JavaVM};

pub(crate) struct Interruption {
    vm: JavaVM,
    thread: GlobalRef,
    is_interrupted: JMethodID,
}

impl Interruption {
    pub(crate) fn new(env: &mut JNIEnv) -> jni::errors::Result<Self> {
        let class = env.find_class("java/lang/Thread")?;
        let thread = env
            .call_static_method(&class, "currentThread", "()Ljava/lang/Thread;", &[])?
            .l()?;
        Ok(Self {
            vm: env.get_java_vm()?,
            thread: env.new_global_ref(thread)?,
            is_interrupted: env.get_method_id(class, "isInterrupted", "()Z")?,
        })
    }

    pub(crate) fn is_requested(&self) -> bool {
        // Rayon reuses its workers, so attach each one only once.
        let Ok(mut env) = self.vm.attach_current_thread_as_daemon() else {
            return false;
        };
        // The global thread reference keeps the receiver and method ID valid.
        unsafe {
            env.call_method_unchecked(
                self.thread.as_obj(),
                self.is_interrupted,
                ReturnType::Primitive(Primitive::Boolean),
                &[],
            )
        }
        .and_then(|value| value.z())
        .unwrap_or(false)
    }

    pub(crate) fn throw(&self, env: &mut JNIEnv) -> bool {
        if self.is_requested() {
            let _ = env.throw_new("java/lang/InterruptedException", "Encoding interrupted");
            return true;
        }
        false
    }
}

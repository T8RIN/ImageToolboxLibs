use std::cell::RefCell;

use jni::objects::{GlobalRef, JMethodID};
use jni::signature::{Primitive, ReturnType};
use jni::{JNIEnv, JavaVM};

struct Interruption {
    vm: JavaVM,
    thread: GlobalRef,
    is_interrupted: JMethodID,
}

impl Interruption {
    fn new(env: &mut JNIEnv) -> jni::errors::Result<Self> {
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

    fn is_requested(&self) -> bool {
        let Ok(mut env) = self.vm.get_env() else {
            return false;
        };
        if env.exception_check().unwrap_or(true) {
            return false;
        }
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

    fn throw(&self, env: &mut JNIEnv) -> bool {
        if self.is_requested() {
            let _ = env.throw_new("java/lang/InterruptedException", "Filter interrupted");
            return true;
        }
        false
    }
}

thread_local! {
    static CURRENT: RefCell<Option<Interruption>> = const { RefCell::new(None) };
}

pub(crate) fn requested() -> bool {
    CURRENT.with(|current| current.borrow().as_ref().is_some_and(Interruption::is_requested))
}

pub(crate) struct Scope(Option<Interruption>);

impl Scope {
    pub(crate) fn new(env: &mut JNIEnv) -> jni::errors::Result<Self> {
        let interruption = Interruption::new(env)?;
        Ok(Self(CURRENT.with(|current| current.replace(Some(interruption)))))
    }

    pub(crate) fn throw(&self, env: &mut JNIEnv) -> bool {
        CURRENT.with(|current| current.borrow().as_ref().is_some_and(|check| check.throw(env)))
    }
}

impl Drop for Scope {
    fn drop(&mut self) {
        CURRENT.with(|current| current.replace(self.0.take()));
    }
}

package eu.arrowhead.core.serviceregistry.quartz;

import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

public final class AutoWiringSpringBeanQuartzTaskFactory extends SpringBeanJobFactory {

    private AutowireCapableBeanFactory beanFactory;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        beanFactory = applicationContext.getAutowireCapableBeanFactory();
    }

    @Override
    protected Object createJobInstance(final TriggerFiredBundle bundle) throws Exception {

        final Object task = super.createJobInstance(bundle);
        beanFactory.autowireBean(task);
        return task;
    }

}

/*-
 * #%L
 * Alfresco HX Insight Connector
 * %%
 * Copyright (C) 2023 - 2024 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.hxi_connector.prediction_applier.config;

import java.util.Map;
import java.util.Properties;

import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.extensions.webscripts.Status;
import org.springframework.transaction.TransactionManager;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import org.alfresco.hxi_connector.prediction_applier.rest.api.exception.PredictionStateChangedException;
import org.alfresco.hxi_connector.prediction_applier.service.PredictionService;
import org.alfresco.repo.security.permissions.impl.AlwaysProceedMethodInterceptor;
import org.alfresco.rest.framework.core.exceptions.SimpleMappingExceptionResolver;
import org.alfresco.util.BeanExtender;

@Configuration
public class ExtensionConfiguration
{

    @Bean
    public TransactionInterceptor predictionServiceTransaction(TransactionManager transactionManager, @Value("${server.transaction.mode.default}") String transactionMode)
    {
        TransactionInterceptor transactionInterceptor = new TransactionInterceptor();
        transactionInterceptor.setTransactionManager(transactionManager);
        Properties transactionAttributes = new Properties();
        transactionAttributes.setProperty("*", transactionMode);
        transactionInterceptor.setTransactionAttributes(transactionAttributes);

        return transactionInterceptor;
    }

    @Bean
    public AlwaysProceedMethodInterceptor predictionServiceSecurity()
    {
        return new AlwaysProceedMethodInterceptor();
    }

    @Bean
    @Primary
    public PredictionService predictionService(BeanFactory beanFactory, @Qualifier("predictionServiceImpl") PredictionService predictionService)
    {
        ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
        proxyFactoryBean.setBeanFactory(beanFactory);
        proxyFactoryBean.setTarget(predictionService);
        proxyFactoryBean.setInterfaces(PredictionService.class);
        proxyFactoryBean.setInterceptorNames("predictionServiceTransaction", "AuditMethodInterceptor", "exceptionTranslator", "predictionServiceSecurity");

        return (PredictionService) proxyFactoryBean.getObject();
    }

    @Bean
    public SimpleMappingExceptionResolver hxInsightSimpleMappingExceptionResolver()
    {
        SimpleMappingExceptionResolver resolver = new SimpleMappingExceptionResolver();
        resolver.setExceptionMappings(Map.of(PredictionStateChangedException.class.getName(), Status.STATUS_CONFLICT));

        return resolver;
    }

    @Bean
    public BeanExtender hxInsightBeanExtender()
    {
        BeanExtender beanExtender = new BeanExtender();
        beanExtender.setBeanName("simpleMappingExceptionResolverParent");
        beanExtender.setExtendingBeanName("hxInsightSimpleMappingExceptionResolver");

        return beanExtender;
    }
}

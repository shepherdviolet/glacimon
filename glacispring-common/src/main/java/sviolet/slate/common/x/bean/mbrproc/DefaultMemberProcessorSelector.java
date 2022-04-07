package sviolet.slate.common.x.bean.mbrproc;

import java.lang.annotation.Annotation;

/**
 * <p>默认的ImportSelector, 配合EnableMemberProcessor注解开启功能</p>
 * @author S.Violet
 */
public class DefaultMemberProcessorSelector extends MemberProcessorSelector {

    @Override
    protected Class<? extends Annotation> getEnableAnnotationType() {
        return EnableMemberProcessor.class;
    }

}

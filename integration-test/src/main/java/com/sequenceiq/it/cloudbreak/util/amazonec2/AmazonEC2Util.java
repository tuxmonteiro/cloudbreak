package com.sequenceiq.it.cloudbreak.util.amazonec2;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.sequenceiq.it.cloudbreak.SdxClient;
import com.sequenceiq.it.cloudbreak.context.TestContext;
import com.sequenceiq.it.cloudbreak.dto.sdx.SdxTestDto;
import com.sequenceiq.it.cloudbreak.util.amazonec2.action.EC2ClientActions;

@Component
public class AmazonEC2Util {
    @Inject
    private EC2ClientActions ec2ClientActions;

    private AmazonEC2Util() {
    }

    public List<String> listMasterVolumeIds(TestContext testContext, SdxTestDto testDto, SdxClient sdxClient) {
        return ec2ClientActions.getMasterVolumeIds(testContext, testDto, sdxClient);
    }

    public SdxTestDto compareVolumeIds(SdxTestDto sdxTestDto, List<String> actualVolumeIds, List<String> expectedVolumeIds) {
        return ec2ClientActions.compareVolumeIdsAfterRepair(sdxTestDto, actualVolumeIds, expectedVolumeIds);
    }

    public SdxTestDto deleteMasterInstances(TestContext testContext, SdxTestDto testDto, SdxClient sdxClient) {
        return ec2ClientActions.deleteMasterInstances(testContext, testDto, sdxClient);
    }
}

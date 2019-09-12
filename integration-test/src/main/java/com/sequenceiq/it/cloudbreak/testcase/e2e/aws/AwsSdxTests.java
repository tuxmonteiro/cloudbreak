package com.sequenceiq.it.cloudbreak.testcase.e2e.aws;

import static com.sequenceiq.it.cloudbreak.context.RunningParameter.key;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.testng.annotations.Test;

import com.sequenceiq.it.cloudbreak.client.SdxTestClient;
import com.sequenceiq.it.cloudbreak.cloud.HostGroupType;
import com.sequenceiq.it.cloudbreak.context.Description;
import com.sequenceiq.it.cloudbreak.context.TestContext;
import com.sequenceiq.it.cloudbreak.dto.sdx.SdxRepairTestDto;
import com.sequenceiq.it.cloudbreak.dto.sdx.SdxTestDto;
import com.sequenceiq.it.cloudbreak.testcase.e2e.BasicSdxTests;
import com.sequenceiq.it.cloudbreak.util.amazonec2.AmazonEC2Util;
import com.sequenceiq.it.cloudbreak.util.wait.WaitUtil;

public class AwsSdxTests extends BasicSdxTests {
    @Inject
    private SdxTestClient sdxTestClient;

    @Inject
    private AmazonEC2Util amazonEC2Util;

    @Inject
    private WaitUtil waitUtil;

    @Test(dataProvider = TEST_CONTEXT)
    @Description(
            given = "there is a running Cloudbreak, and an SDX cluster in available state",
            when = "recovery called on the MASTER host group, where the EC2 instance had been deleted",
            then = "SDX recovery should be successful, the cluster should be available AND deletable"
    )
    public void testRecoverSDX(TestContext testContext) {
        String sdx = resourcePropertyProvider().getName();
        String hostgroupName = HostGroupType.MASTER.getName();
        String hostgroupState = "UNHEALTHY";
        List<String> actualVolumeIds = new ArrayList<>();
        List<String> expectedVolumeIds = new ArrayList<>();

        testContext
                .given(sdx, SdxTestDto.class)
                .when(sdxTestClient.create(), key(sdx))
                .await(SDX_RUNNING)
                .then((tc, testDto, client) -> {
                    expectedVolumeIds.addAll(amazonEC2Util.listMasterVolumeIds(tc, testDto, client));
                    return testDto;
                })
                .then((tc, testDto, client) -> {
                    return amazonEC2Util.deleteMasterInstances(tc, testDto, client);
                })
                .when(sdxTestClient.sync(), key(sdx))
                .await(SDX_RUNNING)
                .then((tc, testDto, client) -> {
                    return waitUtil.waitForSdxInstanceStatus(testDto, client, hostgroupName, hostgroupState);
                })
                .given(hostgroupName, SdxRepairTestDto.class).withHostGroupName(hostgroupName)
                .then((tc, testDto, client) -> {
                    return sdxTestClient.repair().action(tc, testDto, client);
                })
                .given(sdx, SdxTestDto.class)
                .await(SDX_RUNNING)
                .then((tc, testDto, client) -> {
                    actualVolumeIds.addAll(amazonEC2Util.listMasterVolumeIds(tc, testDto, client));
                    return testDto;
                })
                .then((tc, testDto, client) -> {
                    return amazonEC2Util.compareVolumeIds(testDto, actualVolumeIds, expectedVolumeIds);
                })
                .then((tc, testDto, client) -> {
                    return sdxTestClient.delete().action(tc, testDto, client);
                })
                .await(SDX_DELETED)
                .validate();
    }
}

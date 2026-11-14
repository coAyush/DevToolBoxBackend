package com.devtoolbox.DevToolBoxRevamp.users.service;

import com.cloudinary.utils.ObjectUtils;
import com.devtoolbox.DevToolBoxRevamp.comm.ApiResponse;
import com.devtoolbox.DevToolBoxRevamp.comm.GetDetails;
import com.devtoolbox.DevToolBoxRevamp.comm.ResponseHelper;
import com.devtoolbox.DevToolBoxRevamp.common.CurrentUserPrincipal;
import com.devtoolbox.DevToolBoxRevamp.config.CloudinaryConfig;
import com.devtoolbox.DevToolBoxRevamp.landing.repo.model.Tools;
import com.devtoolbox.DevToolBoxRevamp.users.dto.requestDto.QrCodeRequestDto;
import com.devtoolbox.DevToolBoxRevamp.users.repo.ToolUsageRepository;
import com.devtoolbox.DevToolBoxRevamp.users.repo.model.ToolUsage;
import com.devtoolbox.DevToolBoxRevamp.users.repo.model.tools.QrCode;
import com.devtoolbox.DevToolBoxRevamp.users.repo.userToolsRepo.QrCodeRepo;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class QrCodeService {
    private final CloudinaryConfig cloudConfig;
    private final ToolUsageRepository toolUsageRepository;
    private final QrCodeRepo qrCodeRepo;
    @Transactional(rollbackFor = Exception.class)
    @SneakyThrows
    public ApiResponse generateQr(CurrentUserPrincipal user,QrCodeRequestDto qrCodeDto){
        ToolUsage usage=ToolUsage.builder()
                .userId(user.getUserId())
                .toolId(qrCodeDto.getToolId())
                .toolSubId(qrCodeDto.getToolSubId())
                .build();
        ToolUsage saved=toolUsageRepository.save(usage);
        if(saved==null){
            log.error("ToolUsage not saved for user : {} requesting tool toolId: {} toolsubid : {}", user.getUserId(), qrCodeDto.getToolSubId());
            throw new RuntimeException("ToolUsage not saved");
        }
        QrCode getSavedCode=qrCodeRepo.findByUrl(qrCodeDto.getUrl());
        if(getSavedCode!=null){
            return generateResponse(getSavedCode);
        }
        String url=qrCodeDto.getUrl();
        int width=qrCodeDto.getWidth();
        int height=qrCodeDto.getHeight();
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix qr=qrCodeWriter.encode(url, BarcodeFormat.QR_CODE,width,height);
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(qr,"png",baos);
        byte [] img=baos.toByteArray();
        Map<String,Object>uploadParam= ObjectUtils.asMap(
                "folder","qr_codes",
                "resource_type","image"
        );
        Map result=cloudConfig.cloudinary().uploader().upload(img,uploadParam);
        String securedUrl= result.get("secure_url").toString();
        QrCode generatedCode=QrCode.builder()
                .url(qrCodeDto.getUrl())
                .cloudinaryUrl(securedUrl)
                .build();
        QrCode savedCode=qrCodeRepo.save(generatedCode);
        if(savedCode==null){
            log.error("error in saving qr code {}",savedCode);
            throw new RuntimeException("error in saving qr code");
        }
        return generateResponse(savedCode);
    }
    
    
    private ApiResponse generateResponse(QrCode qrCode){
        return ResponseHelper.commonSuccess("Qr Code generated successfully...", GetDetails.<String>builder()
                        .details(qrCode.getCloudinaryUrl())
                .build());
    }

}

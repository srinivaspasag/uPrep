package com.lms.common.vedantu.Repo;

import com.lms.common.vedantu.mongo.VedantuUniqueCode;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VedantuUniqueCodeRepo extends MongoRepository<VedantuUniqueCode,String >{

    List<VedantuUniqueCode> findAllByTypeAndCode(String type, String uniqueCode);
}

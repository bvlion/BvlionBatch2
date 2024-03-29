package net.ambitious.bvlion.batch2.mapper;

import net.ambitious.bvlion.batch2.entity.Mp3Entity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Mapper
public interface Mp3Mapper {
	boolean hasSongName(@Param("song_name") String songName);

	int nextFileName();

	void mp3insert(
			@Param("file_name") int fileName,
			@Param("song_name") String songName,
			@Param("song_url") String songUrl,
			@Param("volume") int volume
	);

	List<Mp3Entity> allMp3Data();

	String selectSongName(@Param("file_name") int fileName);

	Mp3Entity selectDataFromSongName(@Param("song_name") String songName);
}
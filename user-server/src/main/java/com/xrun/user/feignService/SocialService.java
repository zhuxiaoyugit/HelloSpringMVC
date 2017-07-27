package com.xrun.user.feignService;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "social-server")
// @FeignClient(value="user-server")
public interface SocialService
{
	@RequestMapping(value = "/social/queryIsFriendList?api_version=1_0", method = RequestMethod.POST)
	String queryIsFriend4List(@RequestParam(value = "user_id") String user_id,
			@RequestParam(value = "user_token") String user_token, @RequestParam(value = "user_list") String user_list);


	@RequestMapping(value = "/social/queryIsFriend?api_version=1_0", method = RequestMethod.POST)
	String queryIsFriend(@RequestParam(value = "user_id") String user_id,
			@RequestParam(value = "user_token") String user_token,
			@RequestParam(value = "friend_user_id") String friend_user_id);
	
//	@RequestMapping(value = "/social/queryUserFriend?api_version=1_0", method = RequestMethod.POST)
//	String queryUserFriend(@RequestParam(value = "user_id") String user_id,
//			@RequestParam(value = "user_token") String user_token, @RequestParam(value = "dest_user_id") String dest_user_id,
//			@RequestParam(value = "offset") Integer offset,@RequestParam(value = "length") Integer length);
	
	@RequestMapping(value = "/social/queryAllFriends?api_version=1_0", method = RequestMethod.POST)
	String queryUserFriend(@RequestParam(value = "user_id") String user_id,
			@RequestParam(value = "user_token") String user_token, @RequestParam(value = "dest_user_id") String dest_user_id);
}

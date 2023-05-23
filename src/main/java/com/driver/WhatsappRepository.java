package com.driver;


import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class WhatsappRepository
{

        private HashMap<Group, List<User>> groupUserMap;
        private HashMap<Group, List<Message>> groupMessageMap;
        private HashMap<Message, User> senderMap;
        private HashMap<Group, User> adminMap;
        private HashSet<String> userMobile;
        private int customGroupCount;
        private int messageId;

       public WhatsappRepository()
       {
            this.groupMessageMap = new HashMap<Group ,List<Message>>();
            this.groupUserMap = new HashMap<Group , List<User>>();
            this.senderMap = new HashMap<Message , User>();
            this.adminMap = new HashMap<Group , User>();
            this.userMobile = new HashSet<>();
            this.customGroupCount=0;
            this.messageId = 0;
       }


    public String createUser(String name, String mobile) throws Exception
    {
        if(userMobile.contains(mobile)){
            throw new Exception("User already exists");
        }
            User user = new User(name , mobile);
            userMobile.add(mobile);
            return "SUCCESS";
    }

    public Group createGroup(List<User> users)
    {
        if(users.size() == 2)
        {
            Group group = new Group(users.get(1).getName(),2);
            groupUserMap.put(group,users); // add into hashmap
            groupMessageMap.put(group,new ArrayList<>());
            return group;
        }else {
            customGroupCount++;
            Group group = new Group("Group" + customGroupCount, users.size());
            groupUserMap.put(group, users);
            groupMessageMap.put(group, new ArrayList<>());
            adminMap.put(group, users.get(0));
            return group;
        }
    }

    public int createMessage(String content)
    {
         messageId++;
         Message m = new Message(messageId , content);
         return m.getId();
    }

    public int sendMessage(Message message, User sender, Group group) throws Exception
    {
        if(!groupUserMap.containsKey(group))
         {
            throw new Exception("Group does not exist");
         }
        if(!groupUserMap.get(group).contains(sender))
         {
             throw new Exception("You are not allowed to send message");
         }
          List<Message> messageList = groupMessageMap.get(group);
             messageList.add(message);
             groupMessageMap.put(group,messageList);
             senderMap.put(message,sender);
                return groupMessageMap.get(group).size();
    }

    public String changeAdmin(User approver, User user, Group group) throws Exception
    {
        if(!groupUserMap.containsKey(group))
                {
                    throw new Exception("Group does not exist");
                }
        if(adminMap.get(group) != (approver))
                {
                    throw new Exception("Approver does not have rights");
                }
        if(!groupUserMap.containsKey(user))
                {
                    throw new Exception("User is not a participant");
                }
        adminMap.replace(group,user);
        return "SUCCESS";
    }

    public int removeUser(User user) throws Exception {
        for (Group gp :groupUserMap.keySet())
        {
            List<User> userList = groupUserMap.get(gp);
            if(userList.contains(user))
            {
                 for (User admin : adminMap.values())
                 {
                     if(admin==user)
                         {
                              throw new Exception("Cannot remove admin");
                         }
                 }
                 groupUserMap.get(gp).remove(user);

                 for (Message m : senderMap.keySet())
                 {
                    User u = senderMap.get(m);
                    if(u == user)
                    {
                        senderMap.remove(m);
                        groupMessageMap.get(gp).remove(m);
                        return groupUserMap.get(gp).size() + groupMessageMap.get(gp).size()+ senderMap.size();
                    }
                }
            }
        }
        throw new Exception("User not found");
    }

    public String findMessage(Date start, Date end, int k) throws Exception {
       TreeMap<Integer,String> mp = new TreeMap<>();
       ArrayList<Integer> list = new ArrayList<>();
        for (Message m : senderMap.keySet())
        {
            if(m.getTimestamp().compareTo(start) > 0 && m.getTimestamp().compareTo(end) < 0)
            {
                mp.put(m.getId(),m.getContent());
                list.add(m.getId());
            }
        }
        if(mp.size() < k)
        {
            throw new Exception();
        }
        Collections.sort(list);
        int K = list.get(list.size()-k);

        return mp.get(K);
    }

}
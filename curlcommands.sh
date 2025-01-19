curl -X PUT -H "Authorization: {authorization_value}" -H "Date: {date_value}" -H "x-ms-version: {version_value}" -H "Content-Length: {content_length}" -H "Content-Type: application/xml; charset=UTF-8" -d @tags.xml "https://myaccount.blob.core.windows.net/mycontainer/myblob?comp=tags"

"Authorization: Bearer $token"

your principal should have Storage blob data owner - this is the least previlege role for this operation

# Login to Azure  
az login  
  
# Get a token for the Azure Storage  
token=$(az account get-access-token --resource=https://storage.azure.com/ --query accessToken -o tsv) 

# Create a file called tags.xml with the following content:

<?xml version="1.0" encoding="utf-8"?>  
<Tags>  
    <TagSet>  
        <Tag>  
            <Key>test</Key>  
            <Value>2024-01-05</Value>  
        </Tag>  
    </TagSet>  
</Tags>  

# Set the Date string as a Tag

curl -X PUT \
-H "Authorization: Bearer $token" \
-H "Date: Sat, 11 Jan 2025 01:02:00 GMT" -H "x-ms-version: 2025-01-05" \
-H "Content-Type: application/xml; charset=UTF-8" \
-d @tags.xml "https://{Your Storage Account}.blob.core.windows.net/sample/vectorindex.csv?comp=tags"


"Container level query with and on tags work"
"Note the URL needs to be encoded"
curl -X GET \
-H "Authorization: Bearer $token" \
-H "Date: Sat, 11 Jan 2025 01:02:00 GMT" -H "x-ms-version: 2025-01-05" \
"https://{Your Storage Account}.blob.core.windows.net?comp=blobs&where=%40container%3D%27sample%27%20AND%20test%3D%272024-01-05%27"

"> works"
"Note the URL needs to be encoded"
curl -X GET \
-H "Authorization: Bearer $token" \
-H "Date: Sat, 11 Jan 2025 01:02:00 GMT" -H "x-ms-version: 2025-01-05" \
"https://{Your Storage Account}.blob.core.windows.net?comp=blobs&where=test%3E%272024-01-04%27"

"Another way to do the same"

curl -X GET \
-H "Authorization: Bearer $token" \
-H "Date: Sat, 11 Jan 2025 20:02:00 GMT" -H "x-ms-version: 2025-01-05" \
-H "x-ms-if-tags: \"test\" >= '2024-01-05'" \
"https://{Your Storage Account}.blob.core.windows.net/sample?restype=container&comp=list"


"List the blobs that fit the criteria for pushing to cold tier"
"Update the blobs to respective tiers"
"Valid values are Hot, Cool, Cold, and Archive"
curl -X PUT \
-H "Authorization: Bearer $token" \
-H  "Content-Length: 0"  \
-H "Date: Sat, 11 Jan 2025 20:02:00 GMT" \
-H "x-ms-version: 2025-01-05" \
-H "x-ms-access-tier: Cool" \
"https://{Your Storage Account}.blob.core.windows.net/sample/vectorindex.csv?comp=tier"

#https://github.com/Azure-Samples/AzureStorageSnippets/tree/master/blobs/howto/Java/blob-devguide/blob-devguide-blobs/src/main/java/com/blobs/devguide/blobs









